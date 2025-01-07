package com.nebula.common.common;

import lombok.Data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class BaseResponse<T> {

  private int code;

  private T data;

  private String message;

  @JsonCreator
  public BaseResponse(
      @JsonProperty("code") int code,
      @JsonProperty("data") T data,
      @JsonProperty("message") String message) {
    this.code = code;
    this.data = data;
    this.message = message;
  }

  public BaseResponse(int code, T data) {
    this(code, data, "");
  }

  public BaseResponse(ErrorCode errorCode) {
    this(errorCode.getCode(), null, errorCode.getMessage());
  }
}