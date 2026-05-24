package com.forum.service;

import com.forum.common.ErrorCode;
import com.forum.common.exception.BizException;
import com.forum.entity.User;
import com.forum.mapper.UserMapper;
import com.forum.service.dto.LoginRequest;
import com.forum.service.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserService.login / logout 测试（M1-T9）
 */
@SpringBootTest
@ActiveProfiles("dev")
class UserServiceLoginTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @MockBean
    private MailService mailService;

    private RegisterRequest regReq(String email, String nickname) {
        RegisterRequest r = new RegisterRequest();
        r.setEmail(email);
        r.setPassword("abc123");
        r.setNickname(nickname);
        return r;
    }

    private LoginRequest loginReq(String email, String password) {
        LoginRequest r = new LoginRequest();
        r.setEmail(email);
        r.setPassword(password);
        return r;
    }

    @Test
    @Transactional
    @Rollback
    void login_happyPath_shouldReturnTokenAndUser() {
        Long id = userService.register(regReq("login-ok@example.com", "登录成功"));
        User u = userMapper.selectById(id);
        u.setEmailVerified(1);
        userMapper.updateById(u);

        Map<String, Object> result = userService.login(loginReq("login-ok@example.com", "abc123"));

        assertNotNull(result.get("token"));
        assertNotNull(result.get("user"));
        @SuppressWarnings("unchecked")
        Map<String, Object> userInfo = (Map<String, Object>) result.get("user");
        assertEquals(id, userInfo.get("id"));
        assertEquals("登录成功", userInfo.get("nickname"));
    }

    @Test
    @Transactional
    @Rollback
    void login_wrongPassword_shouldThrow() {
        userService.register(regReq("login-wrong@example.com", "密码错"));
        User u = userMapper.selectByEmail("login-wrong@example.com");
        u.setEmailVerified(1);
        userMapper.updateById(u);

        BizException ex = assertThrows(BizException.class,
                () -> userService.login(loginReq("login-wrong@example.com", "wrongpass")));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
    }

    @Test
    @Transactional
    @Rollback
    void login_bannedUser_shouldThrow() {
        Long id = userService.register(regReq("login-banned@example.com", "封禁用户"));
        User u = userMapper.selectById(id);
        u.setEmailVerified(1);
        u.setStatus(0);
        userMapper.updateById(u);

        BizException ex = assertThrows(BizException.class,
                () -> userService.login(loginReq("login-banned@example.com", "abc123")));
        assertEquals(ErrorCode.ACCOUNT_BANNED.getCode(), ex.getCode());
    }

    @Test
    @Transactional
    @Rollback
    void login_lockedAccount_shouldThrow() {
        Long id = userService.register(regReq("login-locked@example.com", "锁定用户"));
        User u = userMapper.selectById(id);
        u.setEmailVerified(1);
        u.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        userMapper.updateById(u);

        BizException ex = assertThrows(BizException.class,
                () -> userService.login(loginReq("login-locked@example.com", "abc123")));
        assertEquals(ErrorCode.LOGIN_FAIL_LIMIT.getCode(), ex.getCode());
    }

    @Test
    @Transactional
    @Rollback
    void login_5failures_shouldLockAccount() {
        Long id = userService.register(regReq("login-5fail@example.com", "五次失败"));
        User u = userMapper.selectById(id);
        u.setEmailVerified(1);
        userMapper.updateById(u);

        // 连续失败 4 次 → 还没锁定
        for (int i = 0; i < 4; i++) {
            assertThrows(BizException.class,
                    () -> userService.login(loginReq("login-5fail@example.com", "wrong")));
        }
        // 第 5 次失败 → 应该锁定
        assertThrows(BizException.class,
                () -> userService.login(loginReq("login-5fail@example.com", "wrong")));
        // 验证被锁定
        User locked = userMapper.selectById(id);
        assertNotNull(locked.getLockedUntil());
    }

    @Test
    @Transactional
    @Rollback
    void login_unregisteredEmail_shouldThrow() {
        BizException ex = assertThrows(BizException.class,
                () -> userService.login(loginReq("no-such@example.com", "abc")));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
    }

    @Test
    @Transactional
    @Rollback
    void changePassword_happyPath_shouldAllowNewPasswordLogin() {
        Long id = userService.register(regReq("pwd-change@example.com", "改密用户"));
        User u = userMapper.selectById(id);
        u.setEmailVerified(1);
        userMapper.updateById(u);

        userService.changePassword(id, "abc123", "xyz789");

        assertThrows(BizException.class,
                () -> userService.login(loginReq("pwd-change@example.com", "abc123")));
        Map<String, Object> result = userService.login(loginReq("pwd-change@example.com", "xyz789"));
        assertNotNull(result.get("token"));
    }

    @Test
    @Transactional
    @Rollback
    void changePassword_wrongOld_shouldThrow() {
        Long id = userService.register(regReq("pwd-wrong@example.com", "原密错"));
        BizException ex = assertThrows(BizException.class,
                () -> userService.changePassword(id, "wrong", "xyz789"));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("原密码"));
    }
}
