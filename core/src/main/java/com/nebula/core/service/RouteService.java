package com.nebula.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nebula.common.model.entity.Route;

public interface RouteService extends IService<Route> {
  public void validRoute(Route route, boolean add);
}
