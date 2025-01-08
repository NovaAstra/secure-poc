package com.nebula.core.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nebula.common.common.BaseResponse;
import com.nebula.common.common.ErrorCode;
import com.nebula.common.exception.BusinessException;
import com.nebula.common.model.entity.Route;
import com.nebula.common.utils.ResultUtil;
import com.nebula.core.model.dto.route.RouteAddRequest;
import com.nebula.core.service.RouteService;
import com.nebula.core.utils.RedisUtil;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
@RequestMapping("/route")
public class RouteController {

  private final RouteService routeService;

  private final RedisUtil redisUtil;

  private static final String CACHE_KEY_PREFIX_ROUTE = "route:";

  public RouteController(RouteService routeService, RedisUtil redisUtil) {
    this.routeService = routeService;
    this.redisUtil = redisUtil;
  }

  @PostMapping("/add")
  public BaseResponse<Long> addRoute(@RequestBody RouteAddRequest routeAddRequest, HttpServletRequest request) {
    Route route = new Route();
    BeanUtils.copyProperties(routeAddRequest, route);

    boolean result = routeService.save(route);

    if (!result) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR);
    }

    Long newRouteId = route.getId();
    String routeCache = CACHE_KEY_PREFIX_ROUTE + route.getId();
    redisUtil.delete(routeCache);

    return ResultUtil.success(newRouteId);
  }

  @PostMapping("/update")
  public String updateRoute(@RequestBody String entity) {
    return entity;
  }

  @PostMapping("/delete")
  public String deleteRoute(@RequestBody String entity) {
    return entity;
  }

  @GetMapping("/get")
  public String getRouteById(@RequestBody String entity) {
    return entity;
  }

  @GetMapping("/list/page")
  public String routeByPage(@RequestBody String entity) {
    return entity;
  }

  @PostMapping("/online")
  public String onlineRoute(@RequestBody String entity) {
    return entity;
  }

  @PostMapping("/offline")
  public String offlineRoute(@RequestBody String entity) {
    return entity;
  }
}
