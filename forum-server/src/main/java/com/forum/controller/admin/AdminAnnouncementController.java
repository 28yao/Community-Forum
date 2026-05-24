package com.forum.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forum.common.Result;
import com.forum.entity.Announcement;
import com.forum.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 后台公告管理（admin）
 *
 * - GET    /api/admin/announcements?scope=&boardId=&page=&size=
 * - POST   /api/admin/announcements
 * - PUT    /api/admin/announcements/{id}
 * - DELETE /api/admin/announcements/{id}
 */
@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public Result<IPage<Announcement>> list(
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) Long boardId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(announcementService.adminList(scope, boardId, page, size));
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        String scope = (String) body.get("scope");
        Long boardId = body.get("boardId") instanceof Number ? ((Number) body.get("boardId")).longValue() : null;
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        Integer pinned = body.get("pinned") instanceof Number ? ((Number) body.get("pinned")).intValue() : null;
        Integer sortWeight = body.get("sortWeight") instanceof Number ? ((Number) body.get("sortWeight")).intValue() : null;

        Announcement a = announcementService.create(scope, boardId, title, content, pinned, sortWeight, userId, true);
        return Result.success(Map.of("id", a.getId()));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        String title = (String) body.get("title");
        String content = (String) body.get("content");
        Integer pinned = body.get("pinned") instanceof Number ? ((Number) body.get("pinned")).intValue() : null;
        Integer sortWeight = body.get("sortWeight") instanceof Number ? ((Number) body.get("sortWeight")).intValue() : null;

        announcementService.update(id, title, content, pinned, sortWeight, userId, true);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        announcementService.delete(id, userId, true);
        return Result.success();
    }
}
