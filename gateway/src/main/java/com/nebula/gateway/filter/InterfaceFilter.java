package com.nebula.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.server.ServerWebExchange;

import com.nebula.gateway.utils.RedisUtil;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import java.nio.charset.StandardCharsets;

import org.reactivestreams.Publisher;

import cn.hutool.core.text.AntPathMatcher;

import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@CrossOrigin(origins = "*")
public class InterfaceFilter implements GlobalFilter, Ordered {
  private final RedisUtil redisUtil;

  public InterfaceFilter(RedisUtil redisUtil) {
    this.redisUtil = redisUtil;
  }

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

  public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, long interfaceId,
      long userId) {
    try {
      ServerHttpResponse originalResponse = exchange.getResponse(); // 获取原始响应
      DataBufferFactory bufferFactory = originalResponse.bufferFactory(); // 缓存数据的工厂
      HttpStatusCode statusCode = originalResponse.getStatusCode(); // 获取响应状态码

      if (statusCode == HttpStatus.OK) {
        log.info("响应成功, 准备扣减接口调用次数, interfaceId: {}, userId: {}", interfaceId, userId);

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
          @Override
          public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
            if (body instanceof Flux) {
              Flux<? extends DataBuffer> fluxBody = Flux.from(body);

              return super.writeWith(
                  fluxBody.buffer().flatMap(dataBuffers -> {
                    byte[] content = new byte[dataBuffers.stream().mapToInt(DataBuffer::readableByteCount).sum()];
                    int offset = 0;

                    for (DataBuffer dataBuffer : dataBuffers) {
                      int length = dataBuffer.readableByteCount();
                      dataBuffer.read(content, offset, length);
                      offset += length;

                      DataBufferUtils.release(dataBuffer);
                    }

                    String redisKey = userId + ":invoke:" + interfaceId;
                    log.info("redisKey: {}", redisKey);

                    // boolean delRedisKeySuccess = redisUtil.del(redisKey);
                    // log.info("删除 redisKey, 结果 = {}", delRedisKeySuccess);

                    if (statusCode == HttpStatus.OK) {

                    } else {
                      String errorMessage = "{\"error\": \"参数填写错误，请检查参数\"}";
                      content = errorMessage.getBytes(StandardCharsets.UTF_8);
                    }

                    return Mono.just(bufferFactory.wrap(content));
                  }));
            }

            return super.writeWith(body);
          }
        };

        return chain.filter(exchange.mutate().response(decoratedResponse).build());
      }

      return chain.filter(exchange);
    } catch (Exception e) {
      log.error("网关处理异常", e);
      return chain.filter(exchange);
    }
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
