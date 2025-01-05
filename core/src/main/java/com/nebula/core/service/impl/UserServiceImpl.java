package com.nebula.core.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nebula.common.common.ErrorCode;
import com.nebula.common.exception.BusinessException;
import com.nebula.common.model.entity.User;
import com.nebula.core.mapper.UserMapper;
import com.nebula.core.service.UserService;
import com.nebula.core.utils.RedisUtil;

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
  public User getInvokeUser(String accessKey) {
    if (StrUtil.isBlank(accessKey)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    return userMapper.selectOne(new QueryWrapper<User>().lambda()
        .eq(User::getAccessKey, accessKey));
  }

  @Override
  public User getUserByToken(String token) {
    if (StrUtil.isBlank(token)) {
      throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
    }

    // 从 Redis 中获取用户信息
    String userJson = (String) redisUtil.get("session:" + token);
    if (StrUtil.isBlank(userJson)) {
      throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
    }

    return JSONUtil.toBean(userJson, User.class);
  }

  @Override
  public String userRegister(String userAccount, String userPassword, String confirmPassword, String code) {
    String redisKey = userAccount + ":code";

    if (StrUtil.hasBlank(userAccount, userPassword, confirmPassword)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
    }

    if (!userPassword.equals(confirmPassword)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
    }

    String codeFromRedis = (String) redisUtil.get(redisKey);
    if (!codeFromRedis.equals(code)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
    }

    synchronized (userAccount.intern()) {

      QueryWrapper<User> queryWrapper = new QueryWrapper<>();
      queryWrapper.eq("userAccount", userAccount);
      long count = userMapper.selectCount(queryWrapper);
      if (count > 0) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
      }

      // 2. 加密
      String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
      // 3. 分配 accessKey, secretKey
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
}
