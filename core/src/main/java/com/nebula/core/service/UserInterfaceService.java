package com.nebula.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nebula.common.model.entity.UserInterface;

public interface UserInterfaceService extends IService<UserInterface>{
  public void validUserInterface(UserInterface userInterface, boolean add);

  public boolean invokeCount(long interfaceId, long userId);

  public boolean hasRemainingCount(long userId, long interfaceId);

  public int getApiRemainingCalls(long interfaceId, long userId);

  public boolean updateRemainingCount(long interfaceId, long userId, int calls, int increment);

  boolean applyForApiCallIncrease(Long userId, Long interfaceId, Integer invokeCount);
}
