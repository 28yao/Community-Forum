package com.forum.controller;

import com.forum.common.Result;
import com.forum.entity.Board;
import com.forum.service.BoardFollowService;
import com.forum.service.BoardService;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 版块控制器（M2-T4/T5 + P2-M3）
 *
 * 路径前缀：/api/boards
 * - GET 接口公开（AuthInterceptor 通过 PUBLIC_BOARD_GET 精细放行）
 * - POST/DELETE 关注类接口需登录
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final BoardFollowService boardFollowService;

    /** GET /api/boards - 启用版块列表 */
    @GetMapping
    public Result<List<Board>> list() {
        return Result.success(boardService.listEnabled());
    }

    /**
     * GET /api/boards/recommended?limit=10 - 推荐板块（P2-M3）
     *
     * 公开接口。按 follower_count + post_count 排序。
     */
    @GetMapping("/recommended")
    public Result<List<Board>> recommended(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(boardFollowService.listRecommended(limit));
    }

    /**
     * GET /api/boards/followed - 我关注的板块（P2-M3）
     *
     * 需登录。匿名访问由 AuthInterceptor 拦回 1001。
     */
    @GetMapping("/followed")
    public Result<List<Board>> followed(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(boardFollowService.listFollowedBoards(userId));
    }

    /**
     * GET /api/boards/{id} - 版块详情
     *
     * 返回结构兼容一期：Board 全部字段直接挂在 data 上。
     * P2-M3 新增 isFollowed 字段（与 Board 字段平铺），匿名 / 未登录始终为 false。
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id, HttpServletRequest request) {
        Board board = boardService.getById(id);
        Long userId = (Long) request.getAttribute("userId");
        // 用 ObjectMapper 把 Board 转成 Map，再平铺加 isFollowed。
        // 替代方案：定义 BoardDetailVO 继承字段；这里选 Map 是为了零侵入一期 Board 实体。
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("id", board.getId());
        data.put("name", board.getName());
        data.put("description", board.getDescription());
        data.put("sortWeight", board.getSortWeight());
        data.put("status", board.getStatus());
        data.put("postCount", board.getPostCount());
        data.put("icon", board.getIcon());
        data.put("slogan", board.getSlogan());
        data.put("tags", board.getTags());
        data.put("ownerUserId", board.getOwnerUserId());
        data.put("followerCount", board.getFollowerCount());
        data.put("createdAt", board.getCreatedAt());
        data.put("updatedAt", board.getUpdatedAt());
        data.put("isFollowed", boardFollowService.isFollowed(id, userId));
        return Result.success(data);
    }

    /** POST /api/boards/{id}/follow - 关注板块（幂等，P2-M3） */
    @PostMapping("/{id}/follow")
    public Result<Map<String, Object>> follow(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boardFollowService.follow(id, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("isFollowed", true);
        return Result.success(data);
    }

    /** DELETE /api/boards/{id}/follow - 取消关注（幂等，P2-M3） */
    @DeleteMapping("/{id}/follow")
    public Result<Map<String, Object>> unfollow(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boardFollowService.unfollow(id, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("isFollowed", false);
        return Result.success(data);
    }

    /**
     * PATCH /api/boards/{id} - 吧主修改板块信息（P2-M8）
     *
     * 仅允许 description/icon/slogan/tags，name 不可改。
     * 权限：吧主或管理员。
     */
    @PatchMapping("/{id}")
    public Result<Void> ownerUpdate(@PathVariable Long id,
                                    @RequestBody Map<String, Object> body,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        boolean isAdmin = "admin".equals(role);
        String description = (String) body.get("description");
        String icon = (String) body.get("icon");
        String slogan = (String) body.get("slogan");
        String tags = (String) body.get("tags");
        boardService.ownerUpdate(id, description, icon, slogan, tags, userId, isAdmin);
        return Result.success("已更新", null);
    }
}
