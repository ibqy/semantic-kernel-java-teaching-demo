package com.xb.semantickernel.common;

/**
 * ApiResponse - 统一 API 响应包装类
 *
 * 使用 Java record 实现不可变的统一响应结构，
 * 包含状态码、消息和数据三个字段。教学中演示
 * 如何用 record 简化 DTO 定义并保证线程安全。
 *
 * @author ibqy
 */
public record ApiResponse<T>(int code, String message, T data) {

    /** 返回成功响应，code 为 0 表示业务正常 */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "ok", data);
    }

    /** 返回错误响应，data 为 null */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
