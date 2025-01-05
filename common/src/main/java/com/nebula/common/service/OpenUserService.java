package com.nebula.common.service;

import com.nebula.common.model.entity.User;

public interface OpenUserService {
  User getInvokeUser(String accessKey);

  User getUserByToken(String token);
}
