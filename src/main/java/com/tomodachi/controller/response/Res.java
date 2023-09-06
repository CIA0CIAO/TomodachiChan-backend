package com.tomodachi.controller.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回结果类
 */
@Data
@AllArgsConstructor
public class Res<T> implements Serializable {
    private Integer code;
    private T data;
    private String message;

    /**
     * 生成成功返回结果类
     *
     * @param data 返回数据
     * @param <T>  返回数据类型
     */
    public static <T> Res<T> success(T data) {
        Integer code = Code.SUCCESS.getCode();
        String message = Code.SUCCESS.getDescription();
        return new Res<>(code, data, message);
    }

    /**
     * 生成失败返回结果类
     *
     * @param errorCode 错误码枚举
     * @param message   错误信息
     */
    public static <T> Res<T> error(Code errorCode, String message) {
        Integer code = errorCode.getCode();
        return new Res<>(code, null, message);
    }
}
