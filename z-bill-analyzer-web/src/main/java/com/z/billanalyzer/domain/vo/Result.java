package com.z.billanalyzer.domain.vo;

public record Result<T>(Integer code, String msg, T data) {
    public static <T> Result<T> success() {
        return new Result<>(200, "", null);
    }

    // 成功响应快捷方法
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "成功", data);
    }

    // 失败响应快捷方法
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}