package com.forum.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.io.Serializable;

/**
 * 统一响应体
 *
 * 对应 plan.md §4.1 约定：{ code, message, data }
 * - code=0 表示成功，其他值表示错误（错误码见 ErrorCode）
 * - data 为 null 时不序列化（JsonInclude.NON_NULL）
 *
 * @param <T> data 类型
 * @author liuxinsi
 * @date 2026-05-23
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务状态码，0 表示成功 */
    private final int code;

    /** 提示信息 */
    private final String message;

    /** 业务数据 */
    private final T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功，无数据 */
    public static <T> Result<T> success() {
        return new Result<>(0, "success", null);
    }

    /** 成功，带数据 */
    public static <T> Result<T> success(T data) {
        return new Result<>(0, "success", data);
    }

    /** 成功，自定义消息与数据 */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(0, message, data);
    }

    /** 失败，自定义错误码与消息 */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    /** 失败，使用 ErrorCode 枚举 */
    public static <T> Result<T> fail(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /** 失败，使用 ErrorCode 枚举 + 自定义消息（覆盖枚举默认消息） */
    public static <T> Result<T> fail(ErrorCode errorCode, String message) {
        return new Result<>(errorCode.getCode(), message, null);
    }

    /** 判断是否成功 */
    public boolean isSuccess() {
        return this.code == 0;
    }
}
