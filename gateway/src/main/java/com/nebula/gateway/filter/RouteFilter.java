package com.nebula.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.server.ServerWebExchange;

import com.nebula.common.model.entity.User;
import com.nebula.client.utils.SignUtil;
import com.nebula.common.service.OpenUserService;
import com.nebula.gateway.utils.RedisUtil;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;

import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@CrossOrigin(origins = "*")
public class RouteFilter implements GlobalFilter, Ordered {

  @DubboReference
  private OpenUserService openUserService;

  private final RedisUtil redisUtil;

  private static final Long NONCE_EXPIRATION = 60 * 1L;
  private static final Long TIMESTAMP_EXPIRATION = 60 * 1L;
  private static final List<String> IP_WHITELIST = Arrays.asList("localhost", "127.0.0.1");

  private static final String APPID_HEADER = "appId"; // 获取 appId
  private static final String ACCESS_KEY_HEADER = "accessKey"; // 获取 accessKey
  private static final String NONCE_HEADER = "nonce"; // 获取 nonce
  private static final String TIMESTAMP_HEADER = "timestamp"; // 获取时间戳
  private static final String SIGN_HEADER = "sign"; // 获取签名
  private static final String BODY_HEADER = "body"; // 获取请求体

  public RouteFilter(RedisUtil redisUtil) {
    this.redisUtil = redisUtil;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String path = request.getPath().value();
    log.info("RouterFilter: 请求路径: {}", path);

    // 1. 请求日志记录
    String method = request.getMethodValue().toString();
    log.info("请求唯一标识：{}", request.getId());
    log.info("请求路径：{}", path);
    log.info("请求方法：{}", method);
    log.info("请求参数：{}", request.getQueryParams());
    InetSocketAddress localAddress = request.getLocalAddress();
    String sourceAddress = (localAddress != null) ? localAddress.getHostString() : "unknown";
    log.info("请求来源地址：{}", sourceAddress);
    log.info("请求远程地址：{}", request.getRemoteAddress());

    ServerHttpResponse response = exchange.getResponse();

    // 2. 访问控制 - 黑白名单
    if (!IP_WHITELIST.contains(sourceAddress)) {
      response.setStatusCode(HttpStatus.FORBIDDEN);
      return response.setComplete();
    }

    // 3. 获取请求头信息
    HttpHeaders headers = request.getHeaders();
    String appId = headers.getFirst(APPID_HEADER);
    String accessKey = headers.getFirst(ACCESS_KEY_HEADER);
    String nonce = headers.getFirst(NONCE_HEADER);
    String timestamp = headers.getFirst(TIMESTAMP_HEADER);
    String sign = headers.getFirst(SIGN_HEADER);
    String body = headers.getFirst(BODY_HEADER);
    log.info("接收到请求头信息: appId = {}, accessKey = {}, nonce = {}, timestamp = {}, sign = {}, body = {}",
        appId, accessKey, nonce, timestamp, sign, body);

    User invokeUser = null;

    try {
      invokeUser = openUserService.getInvokeUser(accessKey);
      log.info("用户信息：{}", invokeUser);
    } catch (Exception e) {
      log.error("获取用户信息失败", e);
    }

    // 如果用户不存在，返回未授权
    if (invokeUser == null) {
      log.error("用户不存在，返回未授权");
      return handleNoAuth(response);
    }

    // 4. 检查 nonce 是否有效
    try {
      long nonceValue = Long.parseLong(nonce);
      if (nonceValue > 10000L) {
        log.error("nonce 超过最大值, 拒绝请求: nonce = {}", nonce);
        return handleNoAuth(response);
      }

      String redisKey = "nonce:" + nonceValue;
      boolean isNonceUnique = redisUtil.setIfAbsent(redisKey, nonceValue, NONCE_EXPIRATION);
      if (!isNonceUnique) {
        log.error("nonce 已被使用, 拒绝请求: nonce = {}", nonce);
        return handleNoAuth(response);
      }
    } catch (NumberFormatException e) {
      log.error("nonce 格式不正确, 拒绝请求: nonce = {}", nonce, e);
      return handleNoAuth(response);
    }

    // 5. 检查时间戳是否超时
    try {
      Long currentTime = System.currentTimeMillis() / 1000;
      if ((currentTime - Long.parseLong(timestamp)) >= TIMESTAMP_EXPIRATION) {
        log.error("请求超时, 时间戳超出有效范围: timestamp = {}", timestamp);
        return handleNoAuth(response);
      }
    } catch (NumberFormatException e) {
      log.error("时间戳格式不正确: timestamp = {}", timestamp, e);
      return handleNoAuth(response);
    }

    String secretKey = invokeUser.getSecretKey();
    String serverSign = SignUtil.genSign(secretKey, appId, timestamp, nonce, body);

    // 6. 验证签名是否匹配
    if (sign == null || !sign.equals(serverSign)) {
      log.error("签名验证失败，客户端签名：{}, 服务器生成签名：{}", sign, serverSign);
      return handleNoAuth(response);
    }

    return chain.filter(exchange);
  }

  public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, long interfaceId,
      long userId) {
    try {
      ServerHttpResponse originalResponse = exchange.getResponse(); // 获取原始响应
      DataBufferFactory bufferFactory = originalResponse.bufferFactory(); // 缓存数据的工厂
      HttpStatus statusCode = originalResponse.getStatusCode(); // 获取响应状态码

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

  public Mono<Void> handleNoAuth(ServerHttpResponse response) {
    response.setStatusCode(HttpStatus.FORBIDDEN);
    return response.setComplete();
  }
}
