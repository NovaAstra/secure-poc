package com.nebula.common.utils;

import com.nebula.common.common.BaseResponse;
import com.nebula.common.common.ErrorCode;

/**
 * 返回工具类
 *
 */
public class ResultUtils {

  /**
   * 成功
   *
   * @param data
   * @param <T>
   * @return
   */
  public static <T> BaseResponse<T> success(T data) {
    return new BaseResponse<>(0, data, "ok");
  }

  /**
   * 失败
   *
   * @param errorCode
   * @return
   */
  public static <T> BaseResponse<T> error(ErrorCode errorCode) {
    return new BaseResponse<>(errorCode);
  }

  /**
   * 失败
   *
   * @param code
   * @param message
   * @return
   */
  public static <T> BaseResponse<T> error(int code, String message) {
    return new BaseResponse<T>(code, null, message);
  }

  /**
   * 失败
   *
   * @param errorCode
   * @return
   */
  public static <T> BaseResponse<T> error(ErrorCode errorCode, String message) {
    return new BaseResponse<T>(errorCode.getCode(), null, message);
  }
}
