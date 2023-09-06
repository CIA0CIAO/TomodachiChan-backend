package com.tomodachi.common.exception;

import com.tomodachi.controller.response.Code;
import com.tomodachi.controller.response.Res;
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
    public Res<Object> businessExceptionHandler(BusinessException e) {
        return Res.error(e.getErrorCode(), e.getMessage());
    }

    /**
     * 未知异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Res<Object> exceptionHandler(Exception e) {
        log.error("未知错误: {}", e.getMessage(), e);
        Code errorCode = Code.SYSTEM_ERROR;
        String message = errorCode.getDescription();
        return Res.error(errorCode, message);
    }
}
