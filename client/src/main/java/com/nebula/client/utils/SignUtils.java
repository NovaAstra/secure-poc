package com.nebula.client.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SignUtils {

  private static final String HMAC_SHA256 = "HmacSHA256";
  private static final String SHA256 = "SHA-256";
  private static final int BUFFER_SIZE = 8192; // 8KB 缓冲区

  /**
   * 生成签名
   *
   * @param secretKey 密钥
   * @param appId     应用ID
   * @param timestamp 时间戳
   * @param nonce     随机数
   * @param body      请求体
   * @return 生成的签名
   */
  public static String genSign(String secretKey, String appId, String timestamp, String nonce, String body) {
    String bodyHash = calculateBodyHash(body);
    String concatenatedString = buildConcatenatedString(appId, timestamp, nonce, bodyHash);
    return generateHmacSHA256(secretKey, concatenatedString);
  }

  /**
   * 计算请求体的SHA-256哈希值（支持大体积数据）
   *
   * @param body 请求体
   * @return SHA-256哈希值的Hex字符串
   */
  private static String calculateBodyHash(String body) {
    if (body == null) {
      body = "";
    }
    try {
      MessageDigest digest = MessageDigest.getInstance(SHA256);
      // 将字符串转换为输入流
      try (InputStream inputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8))) {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        // 逐步读取数据并更新哈希值
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          digest.update(buffer, 0, bytesRead);
        }
      }
      byte[] hash = digest.digest();
      return bytesToHex(hash);
    } catch (NoSuchAlgorithmException | IOException e) {
      throw new RuntimeException("Failed to calculate SHA-256 hash", e);
    }
  }

  /**
   * 拼接参数字符串
   *
   * @param appId     应用ID
   * @param timestamp 时间戳
   * @param nonce     随机数
   * @param bodyHash  请求体的哈希值
   * @return 拼接后的字符串
   */
  private static String buildConcatenatedString(String appId, String timestamp, String nonce, String bodyHash) {
    Map<String, String> params = new TreeMap<>();
    params.put("appId", appId);
    params.put("timestamp", timestamp);
    params.put("nonce", nonce);
    params.put("body", bodyHash);

    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : params.entrySet()) {
      sb.append(entry.getKey())
          .append("=")
          .append(urlEncode(entry.getValue()))
          .append("&");
    }
    return sb.substring(0, sb.length() - 1); // 去掉最后一个 "&"
  }

  /**
   * URL编码
   *
   * @param value 需要编码的值
   * @return 编码后的字符串
   */
  private static String urlEncode(String value) {
    try {
      return URLEncoder.encode(value, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Failed to encode URL", e);
    }
  }

  /**
   * 生成HMAC-SHA256签名
   *
   * @param secretKey 密钥
   * @param data      待签名的数据
   * @return Base64编码的签名
   */
  private static String generateHmacSHA256(String secretKey, String data) {
    try {
      Mac mac = Mac.getInstance(HMAC_SHA256);
      SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
      mac.init(secretKeySpec);
      byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(signatureBytes);
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate HMAC-SHA256 signature", e);
    }
  }

  /**
   * 将字节数组转换为Hex字符串
   *
   * @param bytes 字节数组
   * @return Hex字符串
   */
  private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}
