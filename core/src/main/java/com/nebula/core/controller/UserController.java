package com.nebula.core.controller;

import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nebula.common.common.BaseResponse;
import com.nebula.common.common.ErrorCode;
import com.nebula.common.utils.CookieUtil;
import com.nebula.common.utils.ResultUtil;
import com.nebula.common.exception.BusinessException;
import com.nebula.common.model.entity.User;
import com.nebula.core.model.dto.user.UserLoginRequest;
import com.nebula.core.model.dto.user.UserRegisterRequest;
import com.nebula.core.model.vo.UserSecretVO;
import com.nebula.core.model.vo.UserVO;
import com.nebula.core.service.UserService;
import com.nebula.core.utils.RedisUtil;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

  private final UserService userService;

  private final RedisUtil redisUtil;

  public UserController(UserService userService, RedisUtil redisUtil) {
    this.userService = userService;
    this.redisUtil = redisUtil;
  }

  @PostMapping("/register")
  public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
    if (userRegisterRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    String userAccount = userRegisterRequest.getUserAccount();
    String userPassword = userRegisterRequest.getUserPassword();
    String confirmPassword = userRegisterRequest.getConfirmPassword();

    long result = userService.userRegister(userAccount, userPassword, confirmPassword);

    return ResultUtil.success(result);
  }

  @PostMapping("/login")
  public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletResponse response)
      throws NoSuchAlgorithmException {
    if (userLoginRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    String userAccount = userLoginRequest.getUserAccount();
    String userPassword = userLoginRequest.getUserPassword();
    if (StrUtil.hasBlank(userAccount, userPassword)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    User user = userService.userLogin(userAccount, userPassword);
    String newToken = UUID.randomUUID().toString();

    String redisKey = "token:user:" + user.getId();
    String oldToken = (String) redisUtil.get(redisKey);

    if (oldToken != null) {
      boolean del = redisUtil.delete("session:" + oldToken);
      System.out.println("del = " + del);
    }
    redisUtil.set(redisKey, newToken, 600);
    redisUtil.set("session:" + newToken, JSONUtil.toJsonStr(user), 600);

    CookieUtil.writeLoginToken(newToken, response);

    return ResultUtil.success(user);
  }

  @PostMapping("/logout")
  public BaseResponse<Boolean> userLogout(HttpServletRequest request, HttpServletResponse response) {

    String loginToken = CookieUtil.readLoginToken(request);
    if (StrUtil.isBlank(loginToken)) {
      return ResultUtil.error(400, "用户未登录，不可注销");
    }
    CookieUtil.deleteLoginToken(request, response);

    String userId = request.getHeader("userId");
    String redisKey = "token:user:" + userId;
    redisUtil.delete("session:" + loginToken);
    redisUtil.delete(redisKey);

    return ResultUtil.success(true);
  }

  @GetMapping("/get/login")
  public BaseResponse<UserVO> getLoginUser(HttpServletRequest request) {
    String loginToken = CookieUtil.readLoginToken(request);
    if (StrUtil.isBlank(loginToken)) {
      return ResultUtil.error(402, "用户未登录");
    }

    String userJson = (String) redisUtil.get("session:" + loginToken);

    User user = JSONUtil.toBean(userJson, User.class);
    UserVO userVO = new UserVO();
    BeanUtils.copyProperties(user, userVO);
    return ResultUtil.success(userVO);
  }

  @GetMapping("/key")
  public BaseResponse<UserSecretVO> getKey(HttpServletRequest request) {
    String userId = request.getHeader("userId");
    if (userId == null) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", userId);
    queryWrapper.select("access_key", "secret_key");
    User user = userService.getOne(queryWrapper);
    if (user == null) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }
    UserSecretVO userDevKeyVO = new UserSecretVO();
    userDevKeyVO.setSecretKey(user.getSecretKey());
    userDevKeyVO.setAccessKey(user.getAccessKey());
    return ResultUtil.success(userDevKeyVO);
  }

  @PostMapping("/gen/key")
  public BaseResponse<UserSecretVO> genKey(HttpServletRequest httpServletRequest) {
    UserSecretVO userSecretVO = userService.genKey(httpServletRequest);
    return ResultUtil.success(userSecretVO);
  }
}
