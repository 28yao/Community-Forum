package com.forum.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forum.common.Result;
import com.forum.entity.BoardApplication;
import com.forum.service.BoardApplicationService;
import com.forum.service.dto.BoardApplicationRejectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 板块申请管理端 Controller（P2-M2）
 *
 * 全部需 admin 角色（AdminInterceptor 自动拦截 /api/admin/**）
 */
@RestController
@RequestMapping("/api/admin/board-applications")
@RequiredArgsConstructor
public class AdminBoardApplicationController {

    private final BoardApplicationService applicationService;

    /** GET /api/admin/board-applications?status=1&page=1&size=20 - 审核列表 */
    @GetMapping
    public Result<IPage<BoardApplication>> list(@RequestParam(required = false) Integer status,
                                                @RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "20") long size) {
        return Result.success(applicationService.listForReview(status, page, size));
    }

    /** POST /api/admin/board-applications/{id}/approve - 审核通过 */
    @PostMapping("/{id}/approve")
    public Result<Map<String, Object>> approve(@PathVariable Long id, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        Long boardId = applicationService.approve(adminId, id);
        Map<String, Object> data = new HashMap<>();
        data.put("boardId", boardId);
        return Result.success("已通过，板块已创建", data);
    }

    /** POST /api/admin/board-applications/{id}/reject - 审核驳回 */
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id,
                               @Valid @RequestBody BoardApplicationRejectRequest req,
                               HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        applicationService.reject(adminId, id, req.getRejectReason());
        return Result.success("已驳回", null);
    }
}
