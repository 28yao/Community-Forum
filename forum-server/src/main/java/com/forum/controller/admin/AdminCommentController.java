package com.forum.controller.admin;

import com.forum.common.PageResult;
import com.forum.common.Result;
import com.forum.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 后台评论管理（M6-T12）
 */
@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(value = "postId", required = false) Long postId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return Result.success(commentService.adminListComments(postId, page, size));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        commentService.deleteComment(id, operatorId, true);
        return Result.success("已删除", null);
    }

    @PostMapping("/{id}/restore")
    public Result<Void> restore(@PathVariable Long id, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        commentService.restoreComment(id, operatorId);
        return Result.success("已恢复", null);
    }
}
