package com.forum.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forum.common.Result;
import com.forum.entity.BoardApplication;
import com.forum.service.BoardApplicationService;
import com.forum.service.RateLimitService;
import com.forum.service.dto.BoardApplicationSubmitRequest;
import com.forum.service.dto.EligibilityVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 板块申请用户端 Controller（P2-M2）
 *
 * 全部需登录（AuthInterceptor 自动拦截 /api/board-applications/**）
 */
@RestController
@RequestMapping("/api/board-applications")
@RequiredArgsConstructor
public class BoardApplicationController {

    private final BoardApplicationService applicationService;
    private final RateLimitService rateLimitService;

    /** GET /api/board-applications/eligibility - 查申请资格 */
    @GetMapping("/eligibility")
    public Result<EligibilityVO> eligibility(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(applicationService.checkEligibility(userId));
    }

    /** POST /api/board-applications - 提交申请 */
    @PostMapping
    public Result<Map<String, Object>> submit(@Valid @RequestBody BoardApplicationSubmitRequest req,
                                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        rateLimitService.checkBoardApplicationSubmit(userId);
        Long id = applicationService.submit(userId, req);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.success("申请已提交，等待管理员审核", data);
    }

    /** GET /api/board-applications/mine - 我的申请列表 */
    @GetMapping("/mine")
    public Result<IPage<BoardApplication>> mine(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "20") long size,
                                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(applicationService.listMine(userId, page, size));
    }
}
