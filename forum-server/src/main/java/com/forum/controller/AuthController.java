package com.forum.controller;

import com.forum.common.Result;
import com.forum.service.UserService;
import com.forum.service.dto.LoginRequest;
import com.forum.service.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 认证模块控制器
 *
 * 路径前缀：/api/auth/*
 * 当前包含：注册（M1-T6）；后续 T7-T8 邮箱验证、T11-T12 登录/登出。
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * POST /api/auth/register
     * 邮箱注册。校验失败抛 2001；邮箱重复 2002；昵称重复 2003。
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterRequest req) {
        userService.register(req);
        return Result.success("注册成功，验证邮件已发送", null);
    }

    /**
     * POST /api/auth/verify-email?token=xxx
     */
    @PostMapping("/verify-email")
    public Result<Void> verifyEmail(@RequestParam("token") String token) {
        userService.verifyEmail(token);
        return Result.success("邮箱验证成功", null);
    }

    /**
     * POST /api/auth/resend-verification?email=xxx
     */
    @PostMapping("/resend-verification")
    public Result<Void> resendVerification(@RequestParam("email") String email) {
        userService.resendVerification(email);
        return Result.success("验证邮件已重新发送", null);
    }

    /**
     * POST /api/auth/login
     * 登录。返回 {token, user}。
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid LoginRequest req) {
        Map<String, Object> data = userService.login(req);
        return Result.success(data);
    }

    /**
     * POST /api/auth/logout
     * 登出。需要 Token（由 AuthInterceptor 注入 userId）。
     * 临时方案：从 Token 解析 userId；T13 完成后改为从 request attribute 取。
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            // 临时解析，T13 完成后从 request attribute 取
            try {
                Long userId = userService.logoutByToken(token);
                return Result.success("已登出", null);
            } catch (Exception ignored) {
            }
        }
        return Result.success("已登出", null);
    }
}
