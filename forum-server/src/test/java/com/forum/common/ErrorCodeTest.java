package com.forum.common;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ErrorCode 单元测试
 *
 * 验收点：
 *  1) 完全覆盖 plan.md §4.1 列出的 17 个错误码
 *  2) code 全局唯一，无重复
 *  3) 关键码值与 plan.md 一致（不能改）
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
class ErrorCodeTest {

    @Test
    void allCodes_shouldBeUnique() {
        Set<Integer> codes = new HashSet<>();
        for (ErrorCode ec : ErrorCode.values()) {
            assertTrue(codes.add(ec.getCode()), "duplicate code: " + ec);
        }
    }

    @Test
    void codeCount_shouldMatchPlan() {
        // SUCCESS + 4 鉴权 + 4 参数/冲突 + 3 资源不存在 + 3 上传 + 1 系统 = 16
        assertEquals(16, ErrorCode.values().length);
    }

    @Test
    void keyCodes_shouldMatchPlanValues() {
        // 关键码值与 plan.md §4.1 严格对齐
        assertEquals(0,    ErrorCode.SUCCESS.getCode());
        assertEquals(1001, ErrorCode.UNAUTHORIZED.getCode());
        assertEquals(1002, ErrorCode.FORBIDDEN.getCode());
        assertEquals(1003, ErrorCode.ACCOUNT_BANNED.getCode());
        assertEquals(1004, ErrorCode.LOGIN_FAIL_LIMIT.getCode());
        assertEquals(2001, ErrorCode.PARAM_INVALID.getCode());
        assertEquals(2002, ErrorCode.EMAIL_ALREADY_REGISTERED.getCode());
        assertEquals(2003, ErrorCode.NICKNAME_EXISTS.getCode());
        assertEquals(2004, ErrorCode.NICKNAME_COOLDOWN.getCode());
        assertEquals(3001, ErrorCode.POST_NOT_FOUND.getCode());
        assertEquals(3002, ErrorCode.COMMENT_NOT_FOUND.getCode());
        assertEquals(3003, ErrorCode.BOARD_NOT_FOUND.getCode());
        assertEquals(4001, ErrorCode.UPLOAD_TYPE_INVALID.getCode());
        assertEquals(4002, ErrorCode.UPLOAD_TOO_LARGE.getCode());
        assertEquals(4003, ErrorCode.UPLOAD_COUNT_EXCEEDED.getCode());
        assertEquals(9999, ErrorCode.SYSTEM_ERROR.getCode());
    }

    @Test
    void allMessages_shouldBeNonBlank() {
        Arrays.stream(ErrorCode.values()).forEach(ec ->
                assertNotNull(ec.getMessage(), "message null for " + ec));
        Arrays.stream(ErrorCode.values()).forEach(ec ->
                assertFalse(ec.getMessage().trim().isEmpty(), "message blank for " + ec));
    }
}
