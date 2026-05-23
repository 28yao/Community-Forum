package com.forum.service;

import com.forum.common.ErrorCode;
import com.forum.common.exception.BizException;
import com.forum.entity.Board;
import com.forum.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 版块业务服务（M2-T3）
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardMapper boardMapper;

    /** 启用版块列表（按 sort_weight DESC） */
    public List<Board> listEnabled() {
        return boardMapper.selectEnabledList();
    }

    /**
     * 版块详情
     *
     * 仅返回启用且未删除版块；不存在/禁用/已删除统一抛 3003。
     */
    public Board getById(Long id) {
        Board b = boardMapper.selectById(id);
        if (b == null) {
            throw new BizException(ErrorCode.BOARD_NOT_FOUND);
        }
        if (!Integer.valueOf(1).equals(b.getStatus())) {
            throw new BizException(ErrorCode.BOARD_NOT_FOUND);
        }
        return b;
    }

    /** 仅用于 M3 帖子创建：不检查 status，仅校验存在 */
    public Board mustExist(Long id) {
        Board b = boardMapper.selectById(id);
        if (b == null) {
            throw new BizException(ErrorCode.BOARD_NOT_FOUND);
        }
        return b;
    }
}
