package com.nebula.core.controller;

import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.nebula.common.common.BaseResponse;
import com.nebula.common.common.ErrorCode;
import com.nebula.common.utils.ResultUtils;
import com.nebula.common.exception.BusinessException;
import com.nebula.core.model.dto.user.UserLoginRequest;
import com.nebula.core.model.dto.user.UserRegisterRequest;
import com.nebula.core.model.vo.UserSecretVO;
import com.nebula.core.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
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

    return ResultUtils.success(result);
  }

  @PostMapping("/login")
  public BaseResponse<Integer> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletResponse response)
      throws NoSuchAlgorithmException {

    return ResultUtils.success(1);
  }

  @PostMapping("/gen/key")
  public BaseResponse<UserSecretVO> genKey() {
   HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    UserSecretVO userSecretVO = userService.genKey(request);
    return ResultUtils.success(userSecretVO);
  }

}
