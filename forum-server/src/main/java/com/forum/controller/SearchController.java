package com.forum.controller;

import com.forum.common.ErrorCode;
import com.forum.common.PageResult;
import com.forum.common.Result;
import com.forum.common.exception.BizException;
import com.forum.service.RateLimitService;
import com.forum.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 搜索 Controller（M5-T4）
 *
 * GET /api/search?q=&page=&size=  公开
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final RateLimitService rateLimitService;

    @GetMapping
    public Result<PageResult<Map<String, Object>>> search(
            @RequestParam("q") String q,
            @RequestParam(value = "type", defaultValue = "post") String type,
            @RequestParam(value = "scope", defaultValue = "both") String scope,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            HttpServletRequest request) {
        // 登录用户按 userId，匿名按 IP
        Object uid = request.getAttribute("userId");
        String identity = uid != null ? "u:" + uid : "ip:" + clientIp(request);
        rateLimitService.checkSearch(identity);

        switch (type) {
            case "post":
                return Result.success(searchService.searchPosts(q, scope, page, size));
            case "board":
                return Result.success(searchService.searchBoards(q, page, size));
            case "user":
                return Result.success(searchService.searchUsers(q, page, size));
            default:
                throw new BizException(ErrorCode.PARAM_INVALID, "type 必须是 post/board/user");
        }
    }

    private String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            int comma = xff.indexOf(',');
            return comma > 0 ? xff.substring(0, comma).trim() : xff.trim();
        }
        return req.getRemoteAddr();
    }
}
