package com.tomodachi.common.exception;

import com.tomodachi.controller.response.ErrorCode;
import com.tomodachi.controller.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理类
 */
@Slf4j
@RestControllerAdvice
public class GlobalExpHandler {
    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<Object> businessExceptionHandler(BusinessException e) {
        return BaseResponse.error(e.getErrorCode(), e.getMessage());
    }

    /**
     * 未知异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BaseResponse<Object> exceptionHandler(Exception e) {
        log.error("未知错误: {}", e.getMessage(), e);
        ErrorCode errorCode = ErrorCode.SYSTEM_ERROR;
        String message = errorCode.getDescription();
        return BaseResponse.error(errorCode, message);
    }
}
