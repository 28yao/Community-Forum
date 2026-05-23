package com.forum.controller;

import com.forum.common.Result;
import com.forum.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 点赞 Controller（M4-T3）
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /** POST /api/posts/{id}/like - 点赞（幂等） */
    @PostMapping("/{id}/like")
    public Result<Map<String, Object>> like(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        likeService.like(id, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("liked", true);
        return Result.success(data);
    }

    /** DELETE /api/posts/{id}/like - 取消点赞（幂等） */
    @DeleteMapping("/{id}/like")
    public Result<Map<String, Object>> unlike(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        likeService.unlike(id, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("liked", false);
        return Result.success(data);
    }
}
