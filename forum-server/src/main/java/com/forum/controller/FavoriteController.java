package com.forum.controller;

import com.forum.common.Result;
import com.forum.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 收藏 Controller（M4-T17）
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{id}/favorite")
    public Result<Map<String, Object>> favorite(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        favoriteService.favorite(id, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("favorited", true);
        return Result.success(data);
    }

    @DeleteMapping("/{id}/favorite")
    public Result<Map<String, Object>> unfavorite(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        favoriteService.unfavorite(id, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("favorited", false);
        return Result.success(data);
    }
}
