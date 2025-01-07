package com.nebula.common.utils;

import com.nebula.common.common.BaseResponse;
import com.nebula.common.common.ErrorCode;

public class ResultUtils {

  public static <T> BaseResponse<T> success(T data) {
    return new BaseResponse<>(0, data, "ok");
  }

  public static <T> BaseResponse<T> success(T data, String message) {
    return new BaseResponse<>(0, data, message);
  }

  public static <T> BaseResponse<T> error(ErrorCode errorCode) {
    return new BaseResponse<>(errorCode.getCode(), null, errorCode.getMessage());
  }

  public static <T> BaseResponse<T> error(ErrorCode errorCode, String message) {
    return new BaseResponse<>(errorCode.getCode(), null, message);
  }

  public static <T> BaseResponse<T> error(int code, String message) {
    return new BaseResponse<>(code, null, message);
  }
}
