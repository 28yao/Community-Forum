package com.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forum.common.ErrorCode;
import com.forum.common.exception.BizException;
import com.forum.entity.Board;
import com.forum.entity.BoardApplication;
import com.forum.entity.Post;
import com.forum.entity.User;
import com.forum.entity.UserBoardFollow;
import com.forum.mapper.BoardApplicationMapper;
import com.forum.mapper.BoardMapper;
import com.forum.mapper.PostMapper;
import com.forum.mapper.UserBoardFollowMapper;
import com.forum.mapper.UserMapper;
import com.forum.service.dto.BoardApplicationSubmitRequest;
import com.forum.service.dto.EligibilityVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

/**
 * 板块申请审核业务服务（P2-M2）
 *
 * 用户端：checkEligibility / submit / listMine
 * 管理端：listForReview / approve / reject
 *
 * 审核通过流程（事务）：
 *   1. SELECT FOR UPDATE 锁申请记录
 *   2. 二次名称冲突检查（防止两个 pending 同名同时通过）
 *   3. INSERT board (owner_user_id = 申请人, follower_count = 1)
 *   4. UPDATE application (status=2, board_id=新id, reviewer_admin_id, reviewed_at)
 *   5. INSERT user_board_follow (申请人自动关注新板块)
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BoardApplicationService {

    /** 申请门槛：注册天数（PRD D1 默认值-可调） */
    private static final int REQUIRED_DAYS = 3;
    /** 申请门槛：发帖数 */
    private static final int REQUIRED_POSTS = 3;

    private final BoardApplicationMapper applicationMapper;
    private final BoardMapper boardMapper;
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final UserBoardFollowMapper followMapper;

    /** 资格校验。返回 VO 让前端可显示具体差距 */
    public EligibilityVO checkEligibility(Long userId) {
        EligibilityVO vo = new EligibilityVO();
        vo.setRequiredDays(REQUIRED_DAYS);
        vo.setRequiredPosts(REQUIRED_POSTS);

        User u = userMapper.selectById(userId);
        if (u == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        long days = ChronoUnit.DAYS.between(u.getCreatedAt(), LocalDateTime.now());
        long postCount = postMapper.selectCount(new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .eq(Post::getStatus, 1));
        long pending = applicationMapper.countPendingByUser(userId);

        vo.setRegisteredDays(days);
        vo.setPostCount(postCount);
        vo.setHasPending(pending > 0);

        if (pending > 0) {
            vo.setEligible(false);
            vo.setReason("已有 1 个待审核申请，请等待审核完成");
        } else if (days < REQUIRED_DAYS) {
            vo.setEligible(false);
            vo.setReason("注册需满 " + REQUIRED_DAYS + " 天，目前 " + days + " 天");
        } else if (postCount < REQUIRED_POSTS) {
            vo.setEligible(false);
            vo.setReason("已发帖需 ≥ " + REQUIRED_POSTS + "，目前 " + postCount + " 篇");
        } else {
            vo.setEligible(true);
        }
        return vo;
    }

    /** 提交申请。校验资格 + 名称冲突 + tags 数量，INSERT 待审核记录 */
    @Transactional(rollbackFor = Exception.class)
    public Long submit(Long userId, BoardApplicationSubmitRequest req) {
        EligibilityVO eligibility = checkEligibility(userId);
        if (!eligibility.isEligible()) {
            throw new BizException(ErrorCode.FORBIDDEN, eligibility.getReason());
        }
        // tags 数量校验（DTO 只能限总长度，不能限个数）
        validateTags(req.getTags());

        // 名称冲突（仅与正常状态板块冲突；已禁用板块不冲突——但一期 board.name 是 UNIQUE，
        // 实际上一旦禁用名字仍占位。这里保守只挡 status=1）
        Board existing = boardMapper.selectOne(new LambdaQueryWrapper<Board>()
                .eq(Board::getName, req.getName())
                .eq(Board::getStatus, 1));
        if (existing != null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "板块名称已被使用");
        }

        BoardApplication app = new BoardApplication();
        app.setApplicantUserId(userId);
        app.setName(req.getName().trim());
        app.setDescription(req.getDescription());
        app.setIcon(req.getIcon());
        app.setSlogan(req.getSlogan());
        app.setTags(req.getTags());
        app.setStatus(1);
        applicationMapper.insert(app);
        log.info("[BOARD_APP] op=SUBMIT user={} app={} name='{}'", userId, app.getId(), req.getName());
        return app.getId();
    }

    /** 我的申请列表（分页） */
    public IPage<BoardApplication> listMine(Long userId, long page, long size) {
        return applicationMapper.selectMine(userId, page, size);
    }

    /** 审核列表（管理端） */
    public IPage<BoardApplication> listForReview(Integer status, long page, long size) {
        return applicationMapper.selectForReview(status, page, size);
    }

    /**
     * 审核通过：创建板块 + 自动关注。
     *
     * 二次名称冲突检查在事务内，与 SELECT FOR UPDATE 配合避免双通过创建同名板块。
     */
    @Transactional(rollbackFor = Exception.class)
    public Long approve(Long adminId, Long applicationId) {
        BoardApplication app = applicationMapper.selectByIdForUpdate(applicationId);
        if (app == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "申请不存在");
        }
        if (!Integer.valueOf(1).equals(app.getStatus())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "申请状态已变化，无法审核");
        }
        // 二次名称冲突（包含已禁用板块——因为 board.name 是数据库 UNIQUE 约束）
        Board duplicated = boardMapper.selectOne(new LambdaQueryWrapper<Board>()
                .eq(Board::getName, app.getName()));
        if (duplicated != null) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "板块名称已被占用，建议驳回此申请并提示申请人换名");
        }

        Board board = new Board();
        board.setName(app.getName());
        board.setDescription(app.getDescription());
        board.setIcon(app.getIcon());
        board.setSlogan(app.getSlogan());
        board.setTags(app.getTags());
        board.setOwnerUserId(app.getApplicantUserId());
        board.setSortWeight(0);
        board.setStatus(1);
        board.setPostCount(0);
        board.setFollowerCount(1);  // 申请人自动关注
        boardMapper.insert(board);

        app.setStatus(2);
        app.setBoardId(board.getId());
        app.setReviewerAdminId(adminId);
        app.setReviewedAt(LocalDateTime.now());
        applicationMapper.updateById(app);

        UserBoardFollow follow = new UserBoardFollow();
        follow.setUserId(app.getApplicantUserId());
        follow.setBoardId(board.getId());
        followMapper.insert(follow);

        log.info("[BOARD_APP] op=APPROVE admin={} app={} → board={} owner={}",
                adminId, applicationId, board.getId(), app.getApplicantUserId());
        return board.getId();
    }

    /** 审核驳回 */
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long adminId, Long applicationId, String reason) {
        BoardApplication app = applicationMapper.selectByIdForUpdate(applicationId);
        if (app == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "申请不存在");
        }
        if (!Integer.valueOf(1).equals(app.getStatus())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "申请状态已变化，无法审核");
        }
        app.setStatus(3);
        app.setRejectReason(reason);
        app.setReviewerAdminId(adminId);
        app.setReviewedAt(LocalDateTime.now());
        applicationMapper.updateById(app);
        log.info("[BOARD_APP] op=REJECT admin={} app={} reason={}", adminId, applicationId, reason);
    }

    /** tags 个数校验（最多 3 个） */
    private void validateTags(String tags) {
        if (tags == null || tags.isEmpty()) return;
        String[] arr = tags.split(",");
        long nonEmpty = Arrays.stream(arr).map(String::trim).filter(s -> !s.isEmpty()).count();
        if (nonEmpty > 3) {
            throw new BizException(ErrorCode.PARAM_INVALID, "标签最多 3 个");
        }
        for (String t : arr) {
            String trimmed = t.trim();
            if (!trimmed.isEmpty() && trimmed.length() > 10) {
                throw new BizException(ErrorCode.PARAM_INVALID, "单个标签最多 10 字");
            }
        }
    }
}
