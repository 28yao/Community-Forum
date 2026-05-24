package com.forum.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forum.common.Result;
import com.forum.entity.Announcement;
import com.forum.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 前台公告接口
 *
 * - GET /api/announcements?scope=site  — 站点公告（公开）
 * - GET /api/boards/{boardId}/announcements — 板块公告（公开）
 * - POST /api/boards/{boardId}/announcements — 吧主发板块公告（需登录+吧主身份）
 */
@RestController
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /** 站点公告列表（公开） */
    @GetMapping("/api/announcements")
    public Result<IPage<Announcement>> listSite(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(announcementService.listSite(page, size));
    }

    /** 板块公告列表（公开） */
    @GetMapping("/api/boards/{boardId}/announcements")
    public Result<IPage<Announcement>> listBoard(
            @PathVariable Long boardId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(announcementService.listBoard(boardId, page, size));
    }

    /** 吧主在自己板块发公告（需登录） */
    @PostMapping("/api/boards/{boardId}/announcements")
    public Result<Map<String, Object>> createBoardAnnouncement(
            @PathVariable Long boardId,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        boolean isAdmin = "admin".equals(role);

        String title = (String) body.get("title");
        String content = (String) body.get("content");
        Integer pinned = body.get("pinned") instanceof Number ? ((Number) body.get("pinned")).intValue() : null;
        Integer sortWeight = body.get("sortWeight") instanceof Number ? ((Number) body.get("sortWeight")).intValue() : null;

        Announcement a = announcementService.create("board", boardId, title, content, pinned, sortWeight, userId, isAdmin);
        return Result.success(Map.of("id", a.getId()));
    }

    /** 吧主/管理员编辑公告（需登录） */
    @PutMapping("/api/announcements/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        boolean isAdmin = "admin".equals(role);

        String title = (String) body.get("title");
        String content = (String) body.get("content");
        Integer pinned = body.get("pinned") instanceof Number ? ((Number) body.get("pinned")).intValue() : null;
        Integer sortWeight = body.get("sortWeight") instanceof Number ? ((Number) body.get("sortWeight")).intValue() : null;
        Integer status = body.get("status") instanceof Number ? ((Number) body.get("status")).intValue() : null;

        announcementService.update(id, title, content, pinned, sortWeight, status, userId, isAdmin);
        return Result.success();
    }

    /** 吧主/管理员删除公告（需登录） */
    @DeleteMapping("/api/announcements/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        boolean isAdmin = "admin".equals(role);

        announcementService.delete(id, userId, isAdmin);
        return Result.success();
    }
}
