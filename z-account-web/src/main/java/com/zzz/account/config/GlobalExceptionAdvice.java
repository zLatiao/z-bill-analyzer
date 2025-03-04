package com.zzz.account.config;

import com.zzz.account.entity.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {
    // 通用异常捕获
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        if ("No static resource favicon.ico.".equals(e.getMessage())) {
            return null;
        }
        log.error("系统异常：{}", e.getMessage(), e);
        return Result.fail(500, "系统异常：" + e.getMessage());
    }
}
