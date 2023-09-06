package com.tomodachi.controller.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回结果类
 */
@Data
@AllArgsConstructor
public class BaseResponse<T> implements Serializable {
    private Integer code;
    private T data;
    private String message;

    /**
     * 生成成功返回结果类
     *
     * @param data 返回数据
     * @param <T>  返回数据类型
     */
    public static <T> BaseResponse<T> success(T data) {
        Integer code = ErrorCode.SUCCESS.getCode();
        String message = ErrorCode.SUCCESS.getDescription();
        return new BaseResponse<>(code, data, message);
    }

    /**
     * 生成失败返回结果类
     *
     * @param errorCode 错误码枚举
     * @param message   错误信息
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message) {
        Integer code = errorCode.getCode();
        return new BaseResponse<>(code, null, message);
    }
}
