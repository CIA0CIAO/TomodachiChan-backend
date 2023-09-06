package com.tomodachi.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用返回状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {
    SUCCESS(0, "请求成功"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NULL_ERROR(40001, "查询数据为空"),
    NOT_LOGIN_ERROR(40100, "登录状态异常"),
    AUTH_ERROR(40300, "用户权限异常"),
    SYSTEM_ERROR(50000, "系统未知异常");

    private final Integer code;
    private final String description;
}
