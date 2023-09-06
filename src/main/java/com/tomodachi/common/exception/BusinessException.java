package com.tomodachi.common.exception;

import com.tomodachi.controller.response.ErrorCode;
import lombok.Getter;

/**
 * 自定义业务异常类
 */
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String message;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDescription());
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }
}
