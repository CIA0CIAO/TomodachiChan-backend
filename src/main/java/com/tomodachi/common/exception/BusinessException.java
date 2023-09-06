package com.tomodachi.common.exception;

import com.tomodachi.controller.response.Code;
import lombok.Getter;

/**
 * 自定义业务异常类
 */
@Getter
public class BusinessException extends RuntimeException {
    private final Code errorCode;
    private final String message;

    public BusinessException(Code errorCode) {
        this(errorCode, errorCode.getDescription());
    }

    public BusinessException(Code errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }
}
