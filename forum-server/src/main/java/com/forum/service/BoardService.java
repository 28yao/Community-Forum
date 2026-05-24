package com.forum.service;

import com.forum.common.ErrorCode;
import com.forum.common.exception.BizException;
import com.forum.entity.Board;
import com.forum.entity.User;
import com.forum.mapper.BoardMapper;
import com.forum.mapper.UserMapper;
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
    private final UserMapper userMapper;

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

    // ========== P2-M8 吧主权限 ==========

    /**
     * 吧主修改板块信息（P2-M8）
     * 仅允许修改 description/icon/slogan/tags，name 不可改。
     * 权限：当前用户是板块 owner_user_id 或管理员。
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void ownerUpdate(Long boardId, String description, String icon, String slogan, String tags, Long currentUserId, boolean isAdmin) {
        Board b = boardMapper.selectById(boardId);
        if (b == null) {
            throw new BizException(ErrorCode.BOARD_NOT_FOUND);
        }
        // 系统板块 id=1 不可修改
        if (b.getId() == 1L) {
            throw new BizException(ErrorCode.FORBIDDEN, "系统板块不可修改");
        }
        if (!isAdmin && !b.getOwnerUserId().equals(currentUserId)) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        if (description != null) b.setDescription(description);
        if (icon != null) b.setIcon(icon);
        if (slogan != null) b.setSlogan(slogan);
        if (tags != null) b.setTags(tags);
        boardMapper.updateById(b);
        log.info("[OWNER_UPDATE] board={} by={}", boardId, currentUserId);
    }

    /**
     * 移交吧主（P2-M8，仅管理员）
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void transferOwner(Long boardId, Long newOwnerId, Long adminId) {
        Board b = boardMapper.selectById(boardId);
        if (b == null) {
            throw new BizException(ErrorCode.BOARD_NOT_FOUND);
        }
        if (b.getId() == 1L) {
            throw new BizException(ErrorCode.FORBIDDEN, "系统板块不可移交");
        }
        User newOwner = userMapper.selectById(newOwnerId);
        if (newOwner == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "用户不存在");
        }
        if (!Integer.valueOf(1).equals(newOwner.getStatus())) {
            throw new BizException(ErrorCode.FORBIDDEN, "该用户已被封禁");
        }
        boardMapper.updateOwner(boardId, newOwnerId);
        log.info("[TRANSFER_OWNER] board={} from={} to={} by admin={}", boardId, b.getOwnerUserId(), newOwnerId, adminId);
    }

    /** 判断用户是否是板块吧主 */
    public boolean isOwner(Long boardId, Long userId) {
        if (userId == null) return false;
        Board b = boardMapper.selectById(boardId);
        return b != null && userId.equals(b.getOwnerUserId());
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

    /**
     * 删除板块（吧主或管理员）
     * 系统板块(id=1)不可删除。软删除。
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void deleteBoard(Long boardId, Long currentUserId, boolean isAdmin) {
        Board b = boardMapper.selectById(boardId);
        if (b == null) {
            throw new BizException(ErrorCode.BOARD_NOT_FOUND);
        }
        if (b.getId() == 1L) {
            throw new BizException(ErrorCode.FORBIDDEN, "系统板块不可删除");
        }
        if (!isAdmin && !b.getOwnerUserId().equals(currentUserId)) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        boardMapper.deleteById(boardId);
        log.info("[DELETE_BOARD] board={} by={}", boardId, currentUserId);
    }
}
