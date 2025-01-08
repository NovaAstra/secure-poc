package com.nebula.core.service.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.nebula.common.common.ErrorCode;
import com.nebula.common.exception.BusinessException;
import com.nebula.common.model.entity.User;
import com.nebula.core.mapper.UserMapper;
import com.nebula.core.model.vo.UserSecretVO;
import com.nebula.core.service.UserService;
import com.nebula.core.utils.RedisUtil;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

  private final UserMapper userMapper;

  private final RedisUtil redisUtil;

  private static final String SALT = "nebula";

  public UserServiceImpl(UserMapper userMapper, RedisUtil redisUtil) {
    this.userMapper = userMapper;
    this.redisUtil = redisUtil;
  }

  @Override
  public long userRegister(String userAccount, String userPassword, String confirmPassword) {

    if (!userPassword.equals(confirmPassword)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不一致");
    }

    synchronized (userAccount.intern()) {
      QueryWrapper<User> queryWrapper = new QueryWrapper<>();
      queryWrapper.eq("user_account", userAccount);
      long count = userMapper.selectCount(queryWrapper);
      if (count > 0) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
      }

      String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
      String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
      String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));

      User user = new User();
      user.setUserAccount(userAccount);
      user.setUserPassword(encryptPassword);
      user.setAccessKey(accessKey);
      user.setSecretKey(secretKey);
      boolean saveResult = this.save(user);
      if (!saveResult) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
      }
      return user.getId();
    }
  }

  @Override
  public User userLogin(String userAccount, String userPassword) {

    if (StrUtil.hasBlank(userAccount, userPassword)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
    }

    String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("user_account", userAccount);
    queryWrapper.eq("user_password", encryptPassword);
    User user = userMapper.selectOne(queryWrapper);

    if (user == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
    }
    return user;
  }

  @Override
  public User getUserByToken(String token) {
    if (StrUtil.isBlank(token)) {
      throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
    }

    String userJson = Convert.toStr(redisUtil.get("session:" + token));
    if (StrUtil.isBlank(userJson)) {
      throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
    }

    return JSONUtil.toBean(userJson, User.class);
  }


  @Override
  public UserSecretVO genKey(HttpServletRequest request) {
    String userId = request.getHeader("userId");
    if (StrUtil.isBlank(userId)) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户ID不能为空");
    }
    UserSecretVO userSecretVO = this.genKey(userId);
    UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
    updateWrapper.eq("id", userId);
    updateWrapper.set("access_key", userSecretVO.getAccessKey());
    updateWrapper.set("secret_key", userSecretVO.getSecretKey());
    this.update(updateWrapper);
    return userSecretVO;
  }

  private UserSecretVO genKey(String userAccount) {
    String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
    String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
    UserSecretVO userSecretVO = new UserSecretVO();
    userSecretVO.setAccessKey(accessKey);
    userSecretVO.setSecretKey(secretKey);
    return userSecretVO;
  }
}
