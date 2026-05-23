package com.forum.service;

import com.forum.common.exception.BizException;
import com.forum.entity.User;
import com.forum.mapper.UserMapper;
import com.forum.service.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService.resendVerification 测试
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@SpringBootTest
@ActiveProfiles("dev")
class UserServiceResendTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private MailService mailService;

    private RegisterRequest req(String email, String nickname) {
        RegisterRequest r = new RegisterRequest();
        r.setEmail(email);
        r.setPassword("abc123");
        r.setNickname(nickname);
        return r;
    }

    @Test
    @Transactional
    @Rollback
    void resend_happyPath_shouldCallMailService() {
        userService.register(req("t8-ok@example.com", "T8重发测试"));
        // 清掉注册流程已发的 mock 计数
        reset(mailService);
        // 清掉可能的冷却 key
        redisTemplate.delete("mail:resend:cooldown:t8-ok@example.com");

        userService.resendVerification("t8-ok@example.com");

        verify(mailService, times(1)).sendVerificationMail(eq("t8-ok@example.com"), anyString(), anyString());
        // 清理
        redisTemplate.delete("mail:resend:cooldown:t8-ok@example.com");
    }

    @Test
    @Transactional
    @Rollback
    void resend_within60s_shouldHitCooldown() {
        userService.register(req("t8-cool@example.com", "T8冷却测试"));
        reset(mailService);
        redisTemplate.delete("mail:resend:cooldown:t8-cool@example.com");

        userService.resendVerification("t8-cool@example.com");
        // 立刻第二次 → 冷却
        BizException ex = assertThrows(BizException.class,
                () -> userService.resendVerification("t8-cool@example.com"));
        assertEquals(2001, ex.getCode());
        assertTrue(ex.getMessage().contains("60"));

        redisTemplate.delete("mail:resend:cooldown:t8-cool@example.com");
    }

    @Test
    void resend_unregisteredEmail_shouldThrow() {
        BizException ex = assertThrows(BizException.class,
                () -> userService.resendVerification("t8-no-such@example.com"));
        assertEquals(2001, ex.getCode());
    }

    @Test
    @Transactional
    @Rollback
    void resend_alreadyVerified_shouldThrow() {
        Long id = userService.register(req("t8-verified@example.com", "T8已验证"));
        User u = userMapper.selectById(id);
        u.setEmailVerified(1);
        userMapper.updateById(u);
        redisTemplate.delete("mail:resend:cooldown:t8-verified@example.com");

        BizException ex = assertThrows(BizException.class,
                () -> userService.resendVerification("t8-verified@example.com"));
        assertEquals(2001, ex.getCode());
        assertTrue(ex.getMessage().contains("已验证"));
    }
}
