package com.forum.controller;

import com.forum.common.PageResult;
import com.forum.common.Result;
import com.forum.service.PostService;
import com.forum.service.dto.CreatePostRequest;
import com.forum.service.dto.UpdatePostRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 帖子 Controller（M3-T8/T10/T12/T14/T16）
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /** POST /api/posts - 发帖（需登录） */
    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody @Valid CreatePostRequest req,
                                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long postId = postService.createPost(userId, req);
        Map<String, Object> data = new HashMap<>();
        data.put("id", postId);
        return Result.success(data);
    }

    /** GET /api/posts?boardId=&page=&size= - 帖子列表（公开） */
    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(value = "boardId", required = false) Long boardId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return Result.success(postService.listPosts(boardId, page, size));
    }

    /** GET /api/posts/{id} - 帖子详情（公开） */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id, HttpServletRequest request) {
        // 当前公开接口未必带 token；若拦截器没注入 userId 则为 null
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(postService.getPostDetail(id, userId));
    }

    /** PUT /api/posts/{id} - 编辑（仅作者） */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody @Valid UpdatePostRequest req,
                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        postService.updatePost(id, userId, req);
        return Result.success("帖子已更新", null);
    }

    /** DELETE /api/posts/{id} - 软删除（作者或管理员） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        boolean isAdmin = "admin".equals(role);
        postService.deletePost(id, userId, isAdmin);
        return Result.success("帖子已删除", null);
    }
}
