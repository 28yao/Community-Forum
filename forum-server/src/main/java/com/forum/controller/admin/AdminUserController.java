package com.forum.controller.admin;

import com.forum.common.PageResult;
import com.forum.common.Result;
import com.forum.entity.User;
import com.forum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 后台用户管理（M6-T4/T6/T7）
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return Result.success(userService.adminListUsers(keyword, page, size));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        User u = userService.getUserById(id);
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("email", u.getEmail());
        m.put("nickname", u.getNickname());
        m.put("avatar", u.getAvatar());
        m.put("bio", u.getBio());
        m.put("role", u.getRole());
        m.put("status", u.getStatus());
        m.put("banReason", u.getBanReason());
        m.put("emailVerified", u.getEmailVerified());
        m.put("loginFailCount", u.getLoginFailCount());
        m.put("lockedUntil", u.getLockedUntil());
        m.put("createdAt", u.getCreatedAt());
        return Result.success(m);
    }

    /** POST /api/admin/users/{id}/ban  body: { reason } */
    @PostMapping("/{id}/ban")
    public Result<Void> ban(@PathVariable Long id,
                            @RequestBody(required = false) Map<String, String> body,
                            HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        String reason = body == null ? null : body.get("reason");
        userService.banUser(id, operatorId, reason);
        return Result.success("已封禁", null);
    }

    @PostMapping("/{id}/unban")
    public Result<Void> unban(@PathVariable Long id, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        userService.unbanUser(id, operatorId);
        return Result.success("已解封", null);
    }

    /** POST /api/admin/users/{id}/reset-password  body: { newPassword } */
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id,
                                      @RequestBody Map<String, String> body,
                                      HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        userService.adminResetPassword(id, operatorId, body.get("newPassword"));
        return Result.success("密码已重置", null);
    }
}
