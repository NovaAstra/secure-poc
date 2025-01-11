package com.nebula.core.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nebula.common.common.BaseResponse;
import com.nebula.common.common.DeleteRequest;
import com.nebula.common.common.ErrorCode;
import com.nebula.common.exception.BusinessException;
import com.nebula.common.model.entity.Route;
import com.nebula.common.utils.ResultUtil;
import com.nebula.core.model.dto.route.RouteAddRequest;
import com.nebula.core.model.dto.route.RouteUpdateRequest;
import com.nebula.core.service.RouteService;
import com.nebula.core.utils.RedisUtil;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
@RequestMapping("/route")
public class RouteController {

  private final RouteService routeService;

  private final RedisUtil redisUtil;

  private final RedisTemplate<Object, Object> redisTemplate;

  private static final String CACHE_KEY_PREFIX_ROUTE = "route:";

  public RouteController(RouteService routeService, RedisUtil redisUtil, RedisTemplate<Object, Object> redisTemplate) {
    this.routeService = routeService;
    this.redisUtil = redisUtil;
    this.redisTemplate = redisTemplate;
  }

  @PostMapping("/add")
  public BaseResponse<Long> addRoute(@RequestBody RouteAddRequest routeAddRequest, HttpServletRequest request) {
    if (routeAddRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    Route route = new Route();
    BeanUtils.copyProperties(routeAddRequest, route);
    routeService.validRoute(route, true);
    Long userId = Long.valueOf(request.getHeader("userId"));
    route.setUserId(userId);

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
  public BaseResponse<Boolean> updateRoute(@RequestBody RouteUpdateRequest routeUpdateRequest,
      HttpServletRequest request) {
    if (routeUpdateRequest == null || routeUpdateRequest.getId() <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    Route route = new Route();
    BeanUtils.copyProperties(routeUpdateRequest, route);

    routeService.validRoute(route, false);
    Long userId = Long.valueOf(request.getHeader("userId"));

    Route oldRoute = routeService.getById(userId);
    if (oldRoute == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
    }

    if (!oldRoute.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }

    boolean result = routeService.updateById(route);
    String routeCache = CACHE_KEY_PREFIX_ROUTE + oldRoute.getId();
    redisUtil.delete(routeCache);
    return ResultUtil.success(result);
  }

  @PostMapping("/delete")
  public BaseResponse<Boolean> deleteRoute(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
    if (deleteRequest == null || deleteRequest.getId() <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    Long userId = Long.valueOf(request.getHeader("userId"));

    Route oldRoute = routeService.getById(userId);
    if (oldRoute == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
    }

    boolean isDelete = routeService.removeById(userId);
    String routeCache = CACHE_KEY_PREFIX_ROUTE + oldRoute.getId();
    redisUtil.delete(routeCache);
    return ResultUtil.success(isDelete);
  }

  @GetMapping("/get")
  public BaseResponse<Route> getRouteById(long id) {
    if (id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    String cacheKey = CACHE_KEY_PREFIX_ROUTE + id;
    Route route = (Route) redisTemplate.opsForValue().get(cacheKey);
    if (route != null) {
      return ResultUtil.success(route);
    }

    route = routeService.getById(id);

    redisUtil.set(cacheKey, route, 300);
    return ResultUtil.success(route);
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
