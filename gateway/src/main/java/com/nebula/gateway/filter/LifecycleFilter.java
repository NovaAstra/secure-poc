package com.nebula.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.HttpHeaders;

import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

import reactor.core.publisher.Mono;

@Slf4j
@Component
@CrossOrigin(origins = "*")
public class LifecycleFilter implements GlobalFilter, Ordered {
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String webPath = request.getPath().value();
    log.info("LifecycleFilter:请求路径:{}", webPath);

    // 1. 请求日志记录
    String method = request.getMethod().toString();
    log.info("请求唯一标识：{}", request.getId());
    log.info("请求方法：{}", method);
    log.info("请求参数：{}", request.getQueryParams());
    String sourceAddress = request.getLocalAddress().getHostString();
    log.info("请求来源地址：{}", sourceAddress);
    log.info("请求远程地址：{}", request.getRemoteAddress());
    ServerHttpResponse response = exchange.getResponse();

    // 3. 用户鉴权（判断 ak、sk 是否合法）
    HttpHeaders headers = request.getHeaders();
    String accessKey = headers.getFirst("accessKey"); // 获取 accessKey
    String nonce = headers.getFirst("nonce"); // 获取 nonce
    String timestamp = headers.getFirst("timestamp"); // 获取时间戳
    String sign = headers.getFirst("sign"); // 获取签名
    String body = headers.getFirst("body"); // 获取请求体

    log.info("接收到请求的头部信息：accessKey = {}, nonce = {}, timestamp = {}, sign = {}, body = {}",
        accessKey, nonce, timestamp, sign, body);

    return chain.filter(exchange);
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
