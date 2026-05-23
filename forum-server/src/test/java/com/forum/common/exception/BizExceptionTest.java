package com.forum.common.exception;

import com.forum.common.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BizException 单元测试
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
class BizExceptionTest {

    @Test
    void constructFromErrorCode_shouldCopyCodeAndDefaultMessage() {
        BizException e = new BizException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        assertEquals(2002, e.getCode());
        assertEquals("该邮箱已注册，请直接登录", e.getMessage());
    }

    @Test
    void constructFromErrorCodeWithCustomMessage_shouldOverride() {
        BizException e = new BizException(ErrorCode.NICKNAME_COOLDOWN, "下次可修改时间：2026-06-22");
        assertEquals(2004, e.getCode());
        assertEquals("下次可修改时间：2026-06-22", e.getMessage());
    }

    @Test
    void constructFromRawCode_shouldWork() {
        BizException e = new BizException(7777, "test");
        assertEquals(7777, e.getCode());
        assertEquals("test", e.getMessage());
    }

    @Test
    void shouldBeRuntimeException_canBeThrownWithoutDeclaration() {
        // throw without throws clause
        assertThrows(BizException.class, () -> {
            throw new BizException(ErrorCode.POST_NOT_FOUND);
        });
    }
}
