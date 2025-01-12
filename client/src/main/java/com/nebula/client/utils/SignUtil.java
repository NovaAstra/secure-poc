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

public class SignUtil {

  private static final String HMAC_SHA256 = "HmacSHA256";
  private static final String SHA256 = "SHA-256";
  private static final int BUFFER_SIZE = 8192; // 8KB 缓冲区

  public static String genSign(String secretKey, String appId, String timestamp, String nonce, String body) {
    String sortedBody = sortBodyParameters(body); 
    String bodyHash = calculateBodyHash(sortedBody);
    String concatenatedString = buildConcatenatedString(appId, timestamp, nonce, bodyHash);
    return generateHmacSHA256(secretKey, concatenatedString);
  }

  private static String sortBodyParameters(String body) {
    if (body == null || body.isEmpty())
      return "";

    Map<String, String> params = new TreeMap<>();
    String[] pairs = body.split("&");
    for (String pair : pairs) {
      String[] keyValue = pair.split("=");
      if (keyValue.length == 2) {
        params.put(keyValue[0], keyValue[1]);
      }
    }
    StringBuilder sortedBody = new StringBuilder();
    for (Map.Entry<String, String> entry : params.entrySet()) {
      sortedBody.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
    }
    return sortedBody.length() > 0 ? sortedBody.substring(0, sortedBody.length() - 1) : "";
  }

  private static String calculateBodyHash(String body) {
    if (body == null) {
      body = "";
    }
    try {
      MessageDigest digest = MessageDigest.getInstance(SHA256);
      try (InputStream inputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8))) {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
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

  private static String urlEncode(String value) {
    try {
      return URLEncoder.encode(value, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Failed to encode URL", e);
    }
  }

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

  private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}
