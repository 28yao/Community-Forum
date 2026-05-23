package com.forum.controller.admin;

import com.forum.common.Result;
import com.forum.entity.Board;
import com.forum.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台版块管理（M6-T8）
 */
@RestController
@RequestMapping("/api/admin/boards")
@RequiredArgsConstructor
public class AdminBoardController {

    private final BoardService boardService;

    @GetMapping
    public Result<List<Board>> list() {
        return Result.success(boardService.adminListAll());
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        String name = (String) body.get("name");
        String desc = (String) body.get("description");
        Integer sort = body.get("sortWeight") instanceof Number ? ((Number) body.get("sortWeight")).intValue() : null;
        Long id = boardService.createBoard(name, desc, sort, operatorId);
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        return Result.success(m);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        String name = (String) body.get("name");
        String desc = (String) body.get("description");
        Integer sort = body.get("sortWeight") instanceof Number ? ((Number) body.get("sortWeight")).intValue() : null;
        boardService.updateBoard(id, name, desc, sort, operatorId);
        return Result.success("已更新", null);
    }

    @PostMapping("/{id}/status")
    public Result<Void> setStatus(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        int status = ((Number) body.get("status")).intValue();
        boardService.setBoardStatus(id, status, operatorId);
        return Result.success("状态已更新", null);
    }

    /** POST /api/admin/boards/{id}/transfer-owner - 移交吧主（P2-M8） */
    @PostMapping("/{id}/transfer-owner")
    public Result<Void> transferOwner(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        Long newOwnerId = ((Number) body.get("newOwnerId")).longValue();
        boardService.transferOwner(id, newOwnerId, adminId);
        return Result.success("吧主已移交", null);
    }
}
