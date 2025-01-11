package com.nebula.common.service;

import com.nebula.common.model.entity.UserRoute;

public interface OpenUserInterfaceService {
  
  boolean invokeCount(long interfaceId, long userId);

  boolean hasRemainingInvokeCount(long interfaceId, long userId);

  int getApiRemainingCalls(long interfaceId, long userId);

  boolean updateLeftNum(long interfaceId, long userId, int leftNum, int increment);

  UserRoute getUserInterfaceInfo(long userId, long interfaceId);
}
