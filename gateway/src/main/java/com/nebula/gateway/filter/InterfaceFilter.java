package com.nebula.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.server.ServerWebExchange;

import com.nebula.common.model.entity.User;
import com.nebula.gateway.utils.RedisUtils;
import com.nebula.client.utils.SignUtils;
import com.nebula.common.service.OpenUserService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import org.mozilla.universalchardet.UniversalDetector;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.AntPathMatcher;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@CrossOrigin(origins = "*")
public class InterfaceFilter implements GlobalFilter, Ordered {

  private OpenUserService openUserService;

  @Autowired
  private final RedisUtils redisUtil;

  private static final AntPathMatcher pathMatcher = new AntPathMatcher();
  private static final List<String> EXCLUDED_PATHS = CollUtil.newArrayList("/public/**");

  private static final String INTERFACE_HOST = "http://localhost:8092"; // 接口的主机地址
  private static final String ACCESS_KEY_HEADER = "accessKey"; // 获取 accessKey
  private static final String NONCE_HEADER = "nonce"; // 获取 nonce
  private static final String TIMESTAMP_HEADER = "timestamp"; // 获取时间戳
  private static final String SIGN_HEADER = "sign"; // 获取签名
  private static final String BODY_HEADER = "body"; // 获取请求体

  public InterfaceFilter(RedisUtils redisUtil) {
    this.redisUtil = redisUtil;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String webPath = request.getPath().value();
    log.info("请求路径:{}", webPath);

    // 检查当前路径是否匹配不需要过滤的接口前缀
    if (shouldSkipFilter(webPath)) {
      log.debug("Skip成功, 当前路径不需要过滤: {}", webPath);
      return chain.filter(exchange);
    }

    // 1. 请求日志记录
    String path = INTERFACE_HOST + request.getPath().value(); // 请求路径
    String method = request.getMethod().toString(); // 请求方法
    log.info("请求唯一标识：{}", request.getId());
    log.info("请求路径：{}", path);
    log.info("请求方法：{}", method);
    log.info("请求参数：{}", request.getQueryParams());
    String sourceAddress = request.getLocalAddress().getHostString(); // 本地地址
    log.info("请求来源地址：{}", sourceAddress);
    log.info("请求远程地址：{}", request.getRemoteAddress()); // 远程地址
    ServerHttpResponse response = exchange.getResponse();

    // 2. 获取请求头信息
    HttpHeaders headers = request.getHeaders();
    String accessKey = headers.getFirst(ACCESS_KEY_HEADER);
    String nonce = headers.getFirst(NONCE_HEADER);
    String timestamp = headers.getFirst(TIMESTAMP_HEADER);
    String sign = headers.getFirst(SIGN_HEADER);
    String body = headers.getFirst(BODY_HEADER);

    // 3. 校验请求头是否完整
    String missingField = checkForMissingField(accessKey, nonce, timestamp, sign, body);
    if (missingField != null) {
      log.error("请求头信息不完整，路径: {}, 缺失请求头字段: {}", webPath, missingField);
      return handleNoAuth(response);
    }

    // 4. 对 body 进行编码转换（ISO-8859-1 -> UTF-8）
    String convertedBody = convertToUtf8(body);

    // 打印转换后的 body 内容
    log.info("Converted body: {}", convertedBody);

    log.info("接收到请求的头部信息：accessKey = {}, nonce = {}, timestamp = {}, sign = {}, body = {}",
        accessKey, nonce, timestamp, sign, body);

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
        log.error("nonce 超过最大值，拒绝请求：nonce = {}", nonce);
        return handleNoAuth(response);
      }
    } catch (NumberFormatException e) {
      log.error("nonce 格式不正确", e);
      return handleNoAuth(response);
    }

    // 5. 检查时间戳是否超时
    try {
      Long currentTime = System.currentTimeMillis() / 1000;
      final Long FIVE_MINUTES = 60 * 5L;
      if ((currentTime - Long.parseLong(timestamp)) >= FIVE_MINUTES) {
        log.error("请求超时, 时间戳超出有效范围: timestamp = {}", timestamp);
        return handleNoAuth(response);
      }
    } catch (NumberFormatException e) {
      log.error("时间戳格式不正确", e);
      return handleNoAuth(response);
    }

    String secretKey = invokeUser.getSecretKey();
    String serverSign = SignUtils.genSign(secretKey, "mspbots", timestamp, nonce, convertedBody);

    // 6. 验证签名是否匹配
    if (sign == null || !sign.equals(serverSign)) {
      log.error("InterfaceFilter: 签名验证失败，客户端签名：{}, 服务器生成签名：{}", sign, serverSign);
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
    return Ordered.HIGHEST_PRECEDENCE;
  }

  /**
   * 处理未授权的请求
   * 
   * @param response
   * @return
   */
  public Mono<Void> handleNoAuth(ServerHttpResponse response) {
    response.setStatusCode(HttpStatus.FORBIDDEN);
    return response.setComplete();
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
   * 将 body 转换为 UTF-8 编码
   *
   * @param body 待转换的字符串
   * @return 转换后的 UTF-8 编码字符串
   */
  public static String convertToUtf8(String body) {
    if (body == null) {
      return null;
    }

    // 检测编码
    String encoding = detectEncoding(body);
    if (encoding == null) {
      encoding = StandardCharsets.ISO_8859_1.name(); // 如果无法检测编码，默认使用 ISO-8859-1
    }

    // 如果已经是 UTF-8 编码，直接返回
    if (encoding.equalsIgnoreCase(StandardCharsets.UTF_8.name())) {
      return body;
    }

    // 根据检测到的编码转换为 UTF-8
    try {
      byte[] bytes = body.getBytes(Charset.forName(encoding));
      return new String(bytes, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException("Body 编码转换失败", e);
    }
  }

  /**
   * 检测字符串的编码
   *
   * @param str 待检测的字符串
   * @return 检测到的编码名称（如 "UTF-8"、"ISO-8859-1" 等），如果无法检测则返回 null
   */
  public static String detectEncoding(String input) {
    UniversalDetector detector = new UniversalDetector(null);
    byte[] bytes = input.getBytes(StandardCharsets.ISO_8859_1); // 假设原始编码是 ISO-8859-1
    detector.handleData(bytes, 0, bytes.length);
    detector.dataEnd();
    String encoding = detector.getDetectedCharset();
    detector.reset();
    return encoding;
  }

  /**
   * 检查请求头是否完整，返回第一个缺失的字段名称
   *
   * @param accessKey
   * @param nonce
   * @param timestamp
   * @param sign
   * @param body
   * @return 第一个缺失的字段名称，如果所有字段都完整则返回 null
   */
  public static String checkForMissingField(String accessKey, String nonce, String timestamp, String sign,
      String body) {
    return Stream.of(
        Map.entry(accessKey, ACCESS_KEY_HEADER),
        Map.entry(nonce, NONCE_HEADER),
        Map.entry(timestamp, TIMESTAMP_HEADER),
        Map.entry(sign, SIGN_HEADER),
        Map.entry(body, BODY_HEADER))
        .filter(entry -> StrUtil.isEmpty(entry.getKey()))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElse(null);
  }
}
