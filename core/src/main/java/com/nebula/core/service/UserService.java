package com.nebula.core.service;

import javax.servlet.http.HttpServletRequest;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nebula.common.model.entity.User;
import com.nebula.core.model.vo.UserSecretVO;

public interface UserService extends IService<User> {
  public Long userRegister(String userAccount, String userPassword, String confirmPassword);

  public User userLogin(String userAccount, String userPassword);

  public User getUserByToken(String token);

  public UserSecretVO genKey(HttpServletRequest request);
}