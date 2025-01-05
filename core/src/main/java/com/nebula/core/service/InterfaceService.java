package com.nebula.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nebula.common.model.entity.Interface;

public interface InterfaceService extends IService<Interface> {
  public void validInterfaceInfo(Interface uri, boolean add);
}
