package com.forum.common;

import lombok.Getter;

/**
 * 错误码枚举
 *
 * 对应 plan.md §4.1 错误码表。维护时遵循：
 * - 1xxx：鉴权/权限/账号状态
 * - 2xxx：业务参数/资源冲突
 * - 3xxx：业务资源不存在
 * - 4xxx：文件上传相关
 * - 9xxx：系统级
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Getter
public enum ErrorCode {

    // ===== 成功 =====
    SUCCESS(0, "success"),

    // ===== 1xxx 鉴权 / 账号状态 =====
    UNAUTHORIZED(1001, "未登录"),
    FORBIDDEN(1002, "无权限"),
    ACCOUNT_BANNED(1003, "账号已被封禁"),
    LOGIN_FAIL_LIMIT(1004, "登录次数过多，请稍后再试"),

    // ===== 2xxx 参数 / 资源冲突 =====
    PARAM_INVALID(2001, "参数校验失败"),
    EMAIL_ALREADY_REGISTERED(2002, "该邮箱已注册，请直接登录"),
    NICKNAME_EXISTS(2003, "该昵称已被使用"),
    NICKNAME_COOLDOWN(2004, "昵称修改冷却中"),

    // ===== 3xxx 资源不存在 =====
    POST_NOT_FOUND(3001, "帖子不存在"),
    COMMENT_NOT_FOUND(3002, "评论不存在"),
    BOARD_NOT_FOUND(3003, "版块不存在"),

    // ===== 4xxx 文件上传 =====
    UPLOAD_TYPE_INVALID(4001, "图片格式不支持"),
    UPLOAD_TOO_LARGE(4002, "图片过大"),
    UPLOAD_COUNT_EXCEEDED(4003, "图片数量超限"),

    // ===== 9xxx 系统级 =====
    SYSTEM_ERROR(9999, "系统异常");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
