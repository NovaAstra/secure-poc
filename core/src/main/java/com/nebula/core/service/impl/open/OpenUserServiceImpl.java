package com.nebula.core.service.impl.open;

import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nebula.common.common.ErrorCode;
import com.nebula.common.exception.BusinessException;
import com.nebula.common.model.entity.User;
import com.nebula.common.service.OpenUserService;
import com.nebula.core.mapper.UserMapper;
import com.nebula.core.service.UserService;

import cn.hutool.core.util.StrUtil;

public class OpenUserServiceImpl implements OpenUserService {
  @Autowired
  private UserMapper userMapper;

  @Autowired
  private UserService userService;

  @Override
  public User getInvokeUser(String accessKey) {
    if (StrUtil.isBlank(accessKey)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("access_key", accessKey);
    return userMapper.selectOne(queryWrapper);
  }

  @Override
  public User getUserByToken(String token) {
    return userService.getUserByToken(token);
  }

}
