package com.nebula.common.common;

public enum ErrorCode {

  SUCCESS(0, "ok"),
  PARAMS_ERROR(40000, "请求参数错误"),
  NOT_LOGIN_ERROR(40100, "未登录"),
  NO_AUTH_ERROR(40101, "无权限"),
  FORBIDDEN_ERROR(40300, "禁止访问"),
  SYSTEM_ERROR(50000, "系统内部异常");

  private final int code;

  private final String message;

  ErrorCode(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
