package com.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forum.common.exception.BizException;
import com.forum.entity.User;
import com.forum.entity.VerificationToken;
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

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserService.verifyEmail / resendVerification 测试
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@SpringBootTest
@ActiveProfiles("dev")
class UserServiceVerifyTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VerificationTokenMapper tokenMapper;

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
    void verifyEmail_validToken_shouldMarkVerified() {
        Long userId = userService.register(req("t7-ok@example.com", "T7验证测试"));
        VerificationToken t = tokenMapper.selectOne(
                new LambdaQueryWrapper<VerificationToken>().eq(VerificationToken::getUserId, userId));
        assertNotNull(t);

        userService.verifyEmail(t.getToken());

        User u = userMapper.selectById(userId);
        assertEquals(1, u.getEmailVerified());
        VerificationToken used = tokenMapper.selectById(t.getId());
        assertEquals(1, used.getUsed());
    }

    @Test
    void verifyEmail_invalidToken_shouldThrow() {
        BizException ex = assertThrows(BizException.class,
                () -> userService.verifyEmail("no-such-token-xxx"));
        assertEquals(2001, ex.getCode());
    }

    @Test
    @Transactional
    @Rollback
    void verifyEmail_expired_shouldThrow() {
        Long userId = userService.register(req("t7-expired@example.com", "T7过期测试"));
        VerificationToken t = tokenMapper.selectOne(
                new LambdaQueryWrapper<VerificationToken>().eq(VerificationToken::getUserId, userId));
        // 手动改为过期
        t.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        tokenMapper.updateById(t);

        BizException ex = assertThrows(BizException.class,
                () -> userService.verifyEmail(t.getToken()));
        assertEquals(2001, ex.getCode());
        assertTrue(ex.getMessage().contains("过期"));
    }

    @Test
    @Transactional
    @Rollback
    void verifyEmail_alreadyUsed_shouldThrow() {
        Long userId = userService.register(req("t7-used@example.com", "T7已用测试"));
        VerificationToken t = tokenMapper.selectOne(
                new LambdaQueryWrapper<VerificationToken>().eq(VerificationToken::getUserId, userId));
        userService.verifyEmail(t.getToken());          // 第一次成功
        BizException ex = assertThrows(BizException.class,
                () -> userService.verifyEmail(t.getToken()));      // 再次抛错
        assertEquals(2001, ex.getCode());
        assertTrue(ex.getMessage().contains("已使用"));
    }
}
