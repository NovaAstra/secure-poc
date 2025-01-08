package com.nebula.core.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nebula.core.service.RouteService;
import com.nebula.core.utils.RedisUtil;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
@RequestMapping("/route")
public class RouteController {

  private final RouteService routeService;

  private final RedisUtil redisUtil;

  public RouteController(RouteService routeService, RedisUtil redisUtil) {
    this.routeService = routeService;
    this.redisUtil = redisUtil;
  }

  @PostMapping("/add")
  public String addRoute(@RequestBody String entity) {
    return entity;
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
