package com.forum.controller.admin;

import com.forum.common.PageResult;
import com.forum.common.Result;
import com.forum.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 后台帖子管理（M6-T10）
 */
@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
public class AdminPostController {

    private final PostService postService;

    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(value = "boardId", required = false) Long boardId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return Result.success(postService.adminListPosts(boardId, keyword, page, size));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        postService.deletePost(id, operatorId, true);
        return Result.success("已删除", null);
    }

    @PostMapping("/{id}/restore")
    public Result<Void> restore(@PathVariable Long id, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        postService.restorePost(id, operatorId);
        return Result.success("已恢复", null);
    }

    /** POST /api/admin/posts/{id}/pin  body: { pinned: true/false } */
    @PostMapping("/{id}/pin")
    public Result<Void> pin(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        boolean pin = Boolean.TRUE.equals(body.get("pinned"));
        postService.pinPost(id, pin, operatorId);
        return Result.success(pin ? "已置顶" : "已取消置顶", null);
    }
}
