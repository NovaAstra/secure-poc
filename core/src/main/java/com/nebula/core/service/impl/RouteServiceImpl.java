package com.nebula.core.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nebula.common.common.ErrorCode;
import com.nebula.common.exception.BusinessException;
import com.nebula.common.model.entity.Route;
import com.nebula.core.mapper.RouteMapper;
import com.nebula.core.service.RouteService;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RouteServiceImpl extends ServiceImpl<RouteMapper, Route> implements RouteService {
  @Override
  public void validRoute(Route route, boolean add) {
    if (route == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    String name = route.getName();

    if (add) {
      if (StrUtil.hasBlank(name)) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR);
      }
    }

    if (StrUtil.hasBlank(name) && name.length() > 50) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
    }
  }

}
