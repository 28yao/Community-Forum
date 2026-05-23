package com.forum.controller;

import com.forum.common.Result;
import com.forum.entity.User;
import com.forum.service.FileService;
import com.forum.service.UserService;
import com.forum.service.dto.UpdateProfileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户模块控制器（M1-T16/T18）
 *
 * 路径前缀：/api/users/*
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileService fileService;

    /**
     * GET /api/users/{id}
     * 获取用户公开资料（无需登录）
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getUser(@PathVariable Long id) {
        User u = userService.getUserById(id);
        Map<String, Object> data = new HashMap<>();
        data.put("id", u.getId());
        data.put("nickname", u.getNickname());
        data.put("avatar", u.getAvatar());
        data.put("bio", u.getBio());
        data.put("createdAt", u.getCreatedAt());
        return Result.success(data);
    }

    /**
     * PUT /api/users/profile
     * 更新个人资料（需登录）
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody @Valid UpdateProfileRequest req,
                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updateProfile(userId, req.getNickname(), req.getBio());
        return Result.success("资料更新成功", null);
    }

    /**
     * POST /api/users/avatar
     * 上传头像（需登录）
     */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file,
                                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String url = fileService.uploadAvatar(file);
        userService.updateAvatar(userId, url);
        Map<String, String> data = new HashMap<>();
        data.put("avatar", url);
        return Result.success(data);
    }
}
