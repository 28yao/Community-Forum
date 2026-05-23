package com.forum.controller;

import com.forum.common.Result;
import com.forum.entity.Board;
import com.forum.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 版块控制器（M2-T4/T5）
 *
 * 路径前缀：/api/boards
 * 全部公开（AuthInterceptor 已 exclude /api/boards/**）
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /** GET /api/boards - 启用版块列表 */
    @GetMapping
    public Result<List<Board>> list() {
        return Result.success(boardService.listEnabled());
    }

    /** GET /api/boards/{id} - 版块详情 */
    @GetMapping("/{id}")
    public Result<Board> detail(@PathVariable Long id) {
        return Result.success(boardService.getById(id));
    }
}
