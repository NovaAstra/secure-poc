import crypto from "crypto"

export class SignUtil {
  static async genSign(secretKey, appId, timestamp, nonce, body) {
    const sortedBody = SignUtils.sortBody(body); // 对 body 进行排序
    const bodyHash = await SignUtils.calculateBodyHash(sortedBody); // 计算排序后的 body 的哈希值
    const concatenatedString = SignUtils.buildConcatenatedString(appId, timestamp, nonce, bodyHash);
    return SignUtils.generateHmacSHA256(secretKey, concatenatedString);
  }

  // 对 body 进行排序
  static sortBody(body) {
    if (!body) return '';
    const params = new URLSearchParams(body);
    params.sort(); // 对参数进行排序
    return params.toString();
  }

  // 计算 body 的 SHA-256 哈希值
  static async calculateBodyHash(body) {
    if (!body) body = '';
    const encoder = new TextEncoder();
    const data = encoder.encode(body);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    return SignUtils.bytesToHex(new Uint8Array(hashBuffer));
  }

  // 构建签名字符串
  static buildConcatenatedString(appId, timestamp, nonce, bodyHash) {
    const params = new URLSearchParams();
    params.append('appId', appId);
    params.append('timestamp', timestamp);
    params.append('nonce', nonce);
    params.append('body', bodyHash);
    params.sort(); // 对参数进行排序
    return params.toString();
  }

  // 生成 HMAC-SHA256 签名
  static async generateHmacSHA256(secretKey, data) {
    const encoder = new TextEncoder();
    const key = await crypto.subtle.importKey(
      'raw',
      encoder.encode(secretKey),
      { name: 'HMAC', hash: 'SHA-256' },
      false,
      ['sign']
    );
    const signature = await crypto.subtle.sign('HMAC', key, encoder.encode(data));
    return SignUtils.base64Encode(signature);
  }

  // 将字节数组转换为十六进制字符串
  static bytesToHex(bytes) {
    return Array.from(bytes)
      .map((b) => b.toString(16).padStart(2, '0'))
      .join('');
  }

  // 将 ArrayBuffer 转换为 Base64 字符串
  static base64Encode(buffer) {
    const bytes = new Uint8Array(buffer);
    let binary = '';
    bytes.forEach((b) => (binary += String.fromCharCode(b)));
    return btoa(binary);
  }
}