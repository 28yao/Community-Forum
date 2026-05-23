package com.forum.common.exception;

import com.forum.common.ErrorCode;
import com.forum.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理
 *
 * 处理顺序（具体优先）：
 *  1) BizException → 透传业务码
 *  2) 参数校验异常 → 2001 PARAM_INVALID，message 聚合字段错误
 *  3) MaxUploadSizeExceededException → 4002 UPLOAD_TOO_LARGE
 *  4) Throwable 兜底 → 9999 SYSTEM_ERROR（日志记 ERROR 级别）
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：透传业务码 */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e, HttpServletRequest req) {
        log.warn("[BIZ] path={} code={} msg={}", req.getRequestURI(), e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** @RequestBody @Valid 校验失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(this::fieldErrorToString)
                .collect(Collectors.joining("; "));
        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), msg.isEmpty() ? "参数校验失败" : msg);
    }

    /** 表单 @Valid 校验失败 */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(this::fieldErrorToString)
                .collect(Collectors.joining("; "));
        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), msg.isEmpty() ? "参数校验失败" : msg);
    }

    /** @RequestParam / @PathVariable 上的约束校验失败 */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + " " + cv.getMessage())
                .collect(Collectors.joining("; "));
        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), msg);
    }

    /** 上传文件超过 Spring 配置的最大尺寸（multipart 整体限制） */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleUploadTooLarge(MaxUploadSizeExceededException e) {
        log.warn("[UPLOAD_TOO_LARGE] {}", e.getMessage());
        return Result.fail(ErrorCode.UPLOAD_TOO_LARGE);
    }

    /** 兜底：未捕获的异常 */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleThrowable(Throwable e, HttpServletRequest req) {
        log.error("[SYSTEM_ERROR] path={}", req.getRequestURI(), e);
        return Result.fail(ErrorCode.SYSTEM_ERROR);
    }

    private String fieldErrorToString(FieldError fe) {
        return fe.getField() + ": " + fe.getDefaultMessage();
    }
}
