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
   * @param data 返回数据
   * @param <T>  数据类型
   * @return 成功响应
   */
  public static <T> BaseResponse<T> success(T data) {
    return new BaseResponse<>(0, data, "ok");
  }

  /**
   * 成功（带自定义消息）
   *
   * @param data    返回数据
   * @param message 自定义消息
   * @param <T>     数据类型
   * @return 成功响应
   */
  public static <T> BaseResponse<T> success(T data, String message) {
    return new BaseResponse<>(0, data, message);
  }

  /**
   * 失败
   *
   * @param errorCode 错误码
   * @param <T>       数据类型
   * @return 失败响应
   */
  public static <T> BaseResponse<T> error(ErrorCode errorCode) {
    return new BaseResponse<>(errorCode.getCode(), null, errorCode.getMessage());
  }

  /**
   * 失败（带自定义消息）
   *
   * @param errorCode 错误码
   * @param message   自定义消息
   * @param <T>       数据类型
   * @return 失败响应
   */
  public static <T> BaseResponse<T> error(ErrorCode errorCode, String message) {
    return new BaseResponse<>(errorCode.getCode(), null, message);
  }

  /**
   * 失败（自定义错误码和消息）
   *
   * @param code    错误码
   * @param message 自定义消息
   * @param <T>     数据类型
   * @return 失败响应
   */
  public static <T> BaseResponse<T> error(int code, String message) {
    return new BaseResponse<>(code, null, message);
  }
}
