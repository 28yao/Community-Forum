package com.forum.service;

import com.forum.common.ErrorCode;
import com.forum.common.exception.BizException;
import com.forum.entity.User;
import com.forum.mapper.UserMapper;
import com.forum.mapper.VerificationTokenMapper;
import com.forum.service.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService.register 单元测试
 *
 * 邮件发送被 mock，避免真实发件。事务回滚不污染数据库。
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@SpringBootTest
@ActiveProfiles("dev")
class UserServiceRegisterTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VerificationTokenMapper tokenMapper;

    @MockBean
    private MailService mailService;

    private RegisterRequest req(String email, String password, String nickname) {
        RegisterRequest r = new RegisterRequest();
        r.setEmail(email);
        r.setPassword(password);
        r.setNickname(nickname);
        return r;
    }

    @Test
    @Transactional
    @Rollback
    void register_happyPath_shouldInsertUserAndToken() {
        Long id = userService.register(req("t5-happy@example.com", "abc123", "测试T5用户"));
        assertNotNull(id);

        User loaded = userMapper.selectById(id);
        assertEquals("t5-happy@example.com", loaded.getEmail());
        assertEquals("测试T5用户", loaded.getNickname());
        assertEquals("user", loaded.getRole());
        assertEquals(1, loaded.getStatus());
        assertEquals(0, loaded.getEmailVerified());
        // 密码已 bcrypt
        assertTrue(loaded.getPassword().startsWith("$2a$"), "password should be bcrypt-encoded");

        // token 应该被生成
        long tokenCount = tokenMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.forum.entity.VerificationToken>()
                        .eq(com.forum.entity.VerificationToken::getEmail, "t5-happy@example.com"));
        assertEquals(1, tokenCount);

        // 邮件被调用
        verify(mailService, times(1))
                .sendVerificationMail(eq("t5-happy@example.com"), eq("测试T5用户"), anyString());
    }

    @Test
    @Transactional
    @Rollback
    void register_duplicateEmail_shouldThrow2002() {
        // 第一次注册成功
        userService.register(req("t5-dup@example.com", "abc123", "T5重复测试1"));

        // 第二次同邮箱不同昵称 → 2002
        BizException ex = assertThrows(BizException.class,
                () -> userService.register(req("t5-dup@example.com", "xyz789", "T5重复测试2")));
        assertEquals(ErrorCode.EMAIL_ALREADY_REGISTERED.getCode(), ex.getCode());
    }

    @Test
    @Transactional
    @Rollback
    void register_duplicateNickname_shouldThrow2003() {
        userService.register(req("t5-nick1@example.com", "abc123", "T5昵称重复"));

        BizException ex = assertThrows(BizException.class,
                () -> userService.register(req("t5-nick2@example.com", "abc123", "T5昵称重复")));
        assertEquals(ErrorCode.NICKNAME_EXISTS.getCode(), ex.getCode());
    }
}
