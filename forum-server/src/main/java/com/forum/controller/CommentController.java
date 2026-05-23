package com.forum.controller;

import com.forum.common.Result;
import com.forum.service.CommentService;
import com.forum.service.dto.CreateCommentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论 Controller（M4-T9）
 *
 * 路由：
 * - GET  /api/posts/{postId}/comments  公开
 * - POST /api/posts/{postId}/comments  需登录
 * - DELETE /api/comments/{id}          需登录（作者或 admin）
 */
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/api/posts/{postId}/comments")
    public Result<List<Map<String, Object>>> list(@PathVariable Long postId) {
        return Result.success(commentService.listComments(postId));
    }

    @PostMapping("/api/posts/{postId}/comments")
    public Result<Map<String, Object>> create(@PathVariable Long postId,
                                              @RequestBody @Valid CreateCommentRequest req,
                                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long id = commentService.createComment(postId, userId, req.getContent(),
                req.getParentId(), req.getReplyToUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.success(data);
    }

    @DeleteMapping("/api/comments/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        boolean isAdmin = "admin".equals(role);
        commentService.deleteComment(id, userId, isAdmin);
        return Result.success("已删除", null);
    }
}
