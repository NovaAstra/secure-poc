package com.nebula.common.common;

import lombok.Data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE) 
public class BaseResponse<T> implements Serializable {

  private int code;

  private T data;

  private String message;

  public BaseResponse(int code, T data, String message) {
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