package com.forum.common.exception;

import com.forum.common.ErrorCode;
import lombok.Getter;

/**
 * 业务异常
 *
 * Service / Controller 层通过抛出此异常表达业务错误，
 * 由 GlobalExceptionHandler 统一捕获转为 Result.fail 响应。
 *
 * 用法：
 *   throw new BizException(ErrorCode.EMAIL_ALREADY_REGISTERED);
 *   throw new BizException(ErrorCode.NICKNAME_COOLDOWN, "下次可修改时间：2026-06-22");
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 业务错误码 */
    private final int code;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}
