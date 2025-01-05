package com.nebula.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.server.ServerWebExchange;

import com.nebula.common.model.entity.User;
import com.nebula.gateway.utils.RedisUtil;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.reactivestreams.Publisher;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.AntPathMatcher;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@CrossOrigin(origins = "*")
public class InterfaceFilter implements GlobalFilter, Ordered {
  private final RedisUtil redisUtil;

  private static final AntPathMatcher pathMatcher = new AntPathMatcher();
  // 定义不需要过滤的接口前缀
  private static final List<String> EXCLUDED_PATHS = CollUtil.newArrayList(
      "/public/**", // 公共接口
      "/health/**" // 健康检查接口
  );

  private static final String INTERFACE_HOST = "http://localhost:8092"; // 接口的主机地址
  private static final String ACCESS_KEY_HEADER = "accessKey"; // 获取 accessKey
  private static final String NONCE_HEADER = "nonce"; // 获取 nonce
  private static final String TIMESTAMP_HEADER = "timestamp"; // 获取时间戳
  private static final String SIGN_HEADER = "sign"; // 获取签名
  private static final String BODY_HEADER = "body"; // 获取请求体

  public InterfaceFilter(RedisUtil redisUtil) {
    this.redisUtil = redisUtil;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String webPath = request.getPath().value();
    log.info("InterfaceFilter: 请求路径:{}", webPath);

    // 检查当前路径是否匹配不需要过滤的接口前缀
    if (shouldSkipFilter(webPath)) {
      log.debug("InterfaceFilter: Skip成功, 当前路径不需要过滤: {}", webPath);
      return chain.filter(exchange);
    }

    // 1. 请求日志记录
    String path = INTERFACE_HOST + request.getPath().value(); // 请求路径
    String method = request.getMethod().toString(); // 请求方法
    log.info("InterfaceFilter: 请求唯一标识：{}", request.getId());
    log.info("InterfaceFilter: 请求路径：{}", path);
    log.info("InterfaceFilter: 请求方法：{}", method);
    log.info("InterfaceFilter: 请求参数：{}", request.getQueryParams());
    String sourceAddress = request.getLocalAddress().getHostString(); // 本地地址
    log.info("InterfaceFilter: 请求来源地址：{}", sourceAddress);
    log.info("InterfaceFilter: 请求远程地址：{}", request.getRemoteAddress()); // 远程地址
    ServerHttpResponse response = exchange.getResponse();

    // 1. 获取请求头信息
    HttpHeaders headers = request.getHeaders();
    String accessKey = headers.getFirst(ACCESS_KEY_HEADER);
    String nonce = headers.getFirst(NONCE_HEADER);
    String timestamp = headers.getFirst(TIMESTAMP_HEADER);
    String sign = headers.getFirst(SIGN_HEADER);
    String body = headers.getFirst(BODY_HEADER);

    // 2. 校验请求头是否完整，只提示第一个缺失的字段
    String missingField = getMissingField(accessKey, nonce, timestamp, sign, body);
    if (missingField != null) {
      log.error("InterfaceFilter: 请求头信息不完整，路径: {}, 缺失字段: {}", webPath, missingField);
      return handleNoAuth(response);
    }

    // 3. 对 body 进行编码转换（ISO-8859-1 -> UTF-8）
    String convertedBody = convertBodyEncoding(body);

    // 打印转换后的 body 内容
    log.info("InterfaceFilter:  Converted body: {}", convertedBody);

    log.info("InterfaceFilter: 接收到请求的头部信息：accessKey = {}, nonce = {}, timestamp = {}, sign = {}, body = {}",
        accessKey, nonce, timestamp, sign, body);

    User invokeUser = null;

    try {
      log.info("用户信息：{}", invokeUser); // 记录用户信息
    } catch (Exception e) {
      log.error("获取用户信息失败", e);
    }

    String secretKey = invokeUser.getSecretKey();
    String serverSign = SignUtils.genSign(convertedBody, secretKey);

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

  /**
   * 将 body 从 ISO-8859-1 编码转换为 UTF-8 编码
   * 
   * @param body
   * @return
   */
  private String convertBodyEncoding(String body) {
    try {
      return new String(body.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    } catch (Exception e) {
      log.error("InterfaceFilter:  Body 编码转换失败：{}, Reason: {}", body, e.getMessage(), e);
      throw new RuntimeException("Body 编码转换失败", e);
    }
  }

  /**
   * 处理未授权的请求
   * 
   * @param response
   * @return
   */
  public Mono<Void> handleNoAuth(ServerHttpResponse response) {
    response.setStatusCode(HttpStatus.FORBIDDEN); // 设置响应状态为403
    return response.setComplete(); // 完成响应
  }

  /**
   * 检查当前路径是否匹配不需要过滤的接口前缀
   *
   * @param webPath 当前请求路径
   * @return 如果匹配不需要过滤的接口前缀，返回 true；否则返回 false
   */
  private boolean shouldSkipFilter(String webPath) {
    for (String excludedPath : EXCLUDED_PATHS) {
      if (pathMatcher.match(excludedPath, webPath)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 检查请求头是否完整，返回第一个缺失的字段名称
   * 
   * @param accessKey
   * @param nonce
   * @param timestamp
   * @param sign
   * @param body
   * @return
   */
  private String getMissingField(String accessKey, String nonce, String timestamp, String sign, String body) {
    if (ObjectUtil.isNull(accessKey) || accessKey.isEmpty()) {
      return ACCESS_KEY_HEADER;
    }
    if (ObjectUtil.isNull(nonce) || nonce.isEmpty()) {
      return NONCE_HEADER;
    }
    if (ObjectUtil.isNull(timestamp) || timestamp.isEmpty()) {
      return TIMESTAMP_HEADER;
    }
    if (ObjectUtil.isNull(sign) || sign.isEmpty()) {
      return SIGN_HEADER;
    }
    if (ObjectUtil.isNull(body) || body.isEmpty()) {
      return BODY_HEADER;
    }
    return null;
  }
}
