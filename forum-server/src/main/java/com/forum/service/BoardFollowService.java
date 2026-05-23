package com.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forum.common.ErrorCode;
import com.forum.common.exception.BizException;
import com.forum.entity.Board;
import com.forum.entity.UserBoardFollow;
import com.forum.mapper.BoardMapper;
import com.forum.mapper.UserBoardFollowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 板块关注业务服务（P2-M3）
 *
 * 幂等保证：DB 复合主键 (user_id, board_id) + 应用层先查后写 + 并发 UNIQUE 兜底
 * 计数维护：事务内 +1/-1 board.follower_count，仅当实际产生变更时
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BoardFollowService {

    /** 单用户最多关注 50 个板块（PRD D2 默认值-可调） */
    private static final long FOLLOW_LIMIT = 50L;

    private final UserBoardFollowMapper followMapper;
    private final BoardMapper boardMapper;

    /**
     * 关注板块（幂等）。
     *
     * 流程：
     * 1. 校验板块存在（已禁用 status=0 也允许关注，UI 标灰即可，与 prd.md §5.3 一致）
     * 2. 已关注 → 直接返回 true
     * 3. 上限校验（用户关注数 ≥ 50 抛 PARAM_INVALID）
     * 4. INSERT + UPDATE follower_count + 1；DuplicateKey 兜底并发
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean follow(Long boardId, Long userId) {
        Board board = boardMapper.selectById(boardId);
        if (board == null) {
            throw new BizException(ErrorCode.BOARD_NOT_FOUND);
        }
        if (followMapper.selectByUserAndBoard(userId, boardId) != null) {
            return true;
        }
        long current = followMapper.countByUserId(userId);
        if (current >= FOLLOW_LIMIT) {
            throw new BizException(ErrorCode.PARAM_INVALID, "关注数量已达上限（" + FOLLOW_LIMIT + "）");
        }
        try {
            UserBoardFollow f = new UserBoardFollow();
            f.setUserId(userId);
            f.setBoardId(boardId);
            followMapper.insert(f);
            boardMapper.incrFollowerCount(boardId);
        } catch (DuplicateKeyException e) {
            log.warn("[FOLLOW] duplicate insert ignored userId={} boardId={}", userId, boardId);
        }
        return true;
    }

    /** 取消关注（幂等）。删除返回 0 行视为已未关注，不动 follower_count */
    @Transactional(rollbackFor = Exception.class)
    public boolean unfollow(Long boardId, Long userId) {
        Board board = boardMapper.selectById(boardId);
        if (board == null) {
            throw new BizException(ErrorCode.BOARD_NOT_FOUND);
        }
        int affected = followMapper.deleteByUserAndBoard(userId, boardId);
        if (affected > 0) {
            boardMapper.decrFollowerCount(boardId);
        }
        return false;
    }

    /** 是否已关注（供 BoardController.detail 回填 isFollowed） */
    public boolean isFollowed(Long boardId, Long userId) {
        if (boardId == null || userId == null) return false;
        return followMapper.selectByUserAndBoard(userId, boardId) != null;
    }

    /** 用户关注的全部 board_id（供左栏、feed 信息流使用） */
    public List<Long> listFollowedBoardIds(Long userId) {
        if (userId == null) return Collections.emptyList();
        return followMapper.selectBoardIdsByUserId(userId);
    }

    /** 用户关注的板块列表（含完整 board 信息），按关注时间倒序 */
    public List<Board> listFollowedBoards(Long userId) {
        List<Long> ids = listFollowedBoardIds(userId);
        if (ids.isEmpty()) return Collections.emptyList();
        return boardMapper.selectList(new LambdaQueryWrapper<Board>().in(Board::getId, ids));
    }

    /**
     * 推荐板块：按 follower_count DESC + post_count DESC 排序，仅 status=1。
     *
     * 用于左栏"推荐板块"段 + 首页推荐区。Top N 默认 10。
     */
    public List<Board> listRecommended(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return boardMapper.selectList(new LambdaQueryWrapper<Board>()
                .eq(Board::getStatus, 1)
                .orderByDesc(Board::getFollowerCount)
                .orderByDesc(Board::getPostCount)
                .last("LIMIT " + safeLimit));
    }
}
