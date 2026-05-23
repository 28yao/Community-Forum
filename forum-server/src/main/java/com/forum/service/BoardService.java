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

    // ========== M6 后台 ==========

    /** 后台：含禁用版块的全部列表（按 sort_weight DESC） */
    public List<Board> adminListAll() {
        return boardMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Board>()
                .orderByDesc(Board::getSortWeight));
    }

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public Long createBoard(String name, String description, Integer sortWeight, Long operatorId) {
        if (name == null || name.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "版块名称不能为空");
        }
        // 唯一性
        Board exists = boardMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Board>().eq(Board::getName, name));
        if (exists != null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "版块名称已存在");
        }
        Board b = new Board();
        b.setName(name.trim());
        b.setDescription(description == null ? "" : description);
        b.setSortWeight(sortWeight == null ? 0 : sortWeight);
        b.setStatus(1);
        b.setPostCount(0);
        boardMapper.insert(b);
        log.info("[ADMIN] op=CREATE_BOARD by={} board={} name='{}'", operatorId, b.getId(), name);
        return b.getId();
    }

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void updateBoard(Long id, String name, String description, Integer sortWeight, Long operatorId) {
        Board b = boardMapper.selectById(id);
        if (b == null) {
            throw new BizException(ErrorCode.BOARD_NOT_FOUND);
        }
        if (name != null && !name.equals(b.getName())) {
            Board other = boardMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Board>().eq(Board::getName, name));
            if (other != null) {
                throw new BizException(ErrorCode.PARAM_INVALID, "版块名称已存在");
            }
            b.setName(name);
        }
        if (description != null) b.setDescription(description);
        if (sortWeight != null) b.setSortWeight(sortWeight);
        boardMapper.updateById(b);
        log.info("[ADMIN] op=UPDATE_BOARD by={} board={}", operatorId, id);
    }

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void setBoardStatus(Long id, int status, Long operatorId) {
        Board b = boardMapper.selectById(id);
        if (b == null) {
            throw new BizException(ErrorCode.BOARD_NOT_FOUND);
        }
        b.setStatus(status);
        boardMapper.updateById(b);
        log.info("[ADMIN] op=SET_BOARD_STATUS by={} board={} status={}", operatorId, id, status);
    }
}
