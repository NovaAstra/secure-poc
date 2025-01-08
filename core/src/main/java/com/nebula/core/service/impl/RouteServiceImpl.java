package com.nebula.core.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nebula.common.model.entity.Route;
import com.nebula.core.mapper.RouteMapper;
import com.nebula.core.service.RouteService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RouteServiceImpl extends ServiceImpl<RouteMapper, Route> implements RouteService {

}
