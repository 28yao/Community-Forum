package com.forum.controller.admin;

import com.forum.common.ErrorCode;
import com.forum.common.Result;
import com.forum.common.exception.BizException;
import com.forum.service.UserService;
import com.forum.service.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 后台登录（M6-T3）
 *
 * 复用 UserService.login，但要求 role=admin。
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid LoginRequest req) {
        Map<String, Object> data = userService.login(req);
        @SuppressWarnings("unchecked")
        Map<String, Object> userInfo = (Map<String, Object>) data.get("user");
        Object role = userInfo.get("role");
        if (!"admin".equals(role)) {
            // 立刻踢出 token 避免普通用户拿到的 token 滥用
            Object id = userInfo.get("id");
            if (id instanceof Number) {
                userService.logout(((Number) id).longValue());
            }
            throw new BizException(ErrorCode.FORBIDDEN, "无后台权限");
        }
        log.info("[ADMIN] op=ADMIN_LOGIN user={}", userInfo.get("id"));
        return Result.success(data);
    }
}
