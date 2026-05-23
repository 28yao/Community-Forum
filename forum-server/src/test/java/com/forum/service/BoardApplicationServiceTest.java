package com.forum.service;

import com.forum.common.ErrorCode;
import com.forum.common.exception.BizException;
import com.forum.entity.Board;
import com.forum.entity.BoardApplication;
import com.forum.entity.User;
import com.forum.entity.UserBoardFollow;
import com.forum.mapper.BoardApplicationMapper;
import com.forum.mapper.BoardMapper;
import com.forum.mapper.PostMapper;
import com.forum.mapper.UserBoardFollowMapper;
import com.forum.mapper.UserMapper;
import com.forum.service.dto.BoardApplicationSubmitRequest;
import com.forum.service.dto.CreatePostRequest;
import com.forum.service.dto.EligibilityVO;
import com.forum.service.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BoardApplicationService 测试（P2-M2）
 *
 * 覆盖：资格校验 4 个分支 / 提交 3 个分支 / 审核通过 / 驳回 / 名称冲突
 */
@SpringBootTest
@ActiveProfiles("dev")
class BoardApplicationServiceTest {

    @Autowired private BoardApplicationService applicationService;
    @Autowired private BoardApplicationMapper applicationMapper;
    @Autowired private BoardMapper boardMapper;
    @Autowired private UserBoardFollowMapper followMapper;
    @Autowired private UserService userService;
    @Autowired private UserMapper userMapper;
    @Autowired private PostService postService;
    @Autowired private PostMapper postMapper;

    @MockBean private MailService mailService;
    @MockBean private RateLimitService rateLimitService;  // 防止单测里限流干扰

    /** 创建一个用户，注册时长可控（通过事后改 created_at） */
    private Long ensureUser(String email, String nick, int daysAgo) {
        RegisterRequest r = new RegisterRequest();
        r.setEmail(email); r.setPassword("abc123"); r.setNickname(nick);
        Long id = userService.register(r);
        User u = userMapper.selectById(id);
        u.setEmailVerified(1);
        if (daysAgo > 0) u.setCreatedAt(LocalDateTime.now().minusDays(daysAgo));
        userMapper.updateById(u);
        return id;
    }

    /** 给用户造 N 篇帖子（用一期种子板块 id=1） */
    private void createPosts(Long userId, int n) {
        for (int i = 0; i < n; i++) {
            CreatePostRequest r = new CreatePostRequest();
            r.setBoardId(1L);
            r.setTitle("p2m2-test-" + System.nanoTime());
            r.setContent("body");
            postService.createPost(userId, r);
        }
    }

    private BoardApplicationSubmitRequest validRequest(String name) {
        BoardApplicationSubmitRequest r = new BoardApplicationSubmitRequest();
        r.setName(name);
        r.setDescription("二期申请测试用板块描述，至少 10 字");
        r.setSlogan("测试口号");
        r.setTags("测试,板块");
        return r;
    }

    // ===== 资格校验 =====

    @Test
    @Transactional
    @Rollback
    void eligibility_newUser_shouldBeIneligible_days() {
        Long uid = ensureUser("p2m2-elig-new@example.com", "p2m2新", 0);
        EligibilityVO vo = applicationService.checkEligibility(uid);
        assertFalse(vo.isEligible());
        assertTrue(vo.getReason().contains("注册"));
    }

    @Test
    @Transactional
    @Rollback
    void eligibility_oldUserNoPost_shouldBeIneligible_posts() {
        Long uid = ensureUser("p2m2-elig-noposts@example.com", "p2m2无帖", 10);
        EligibilityVO vo = applicationService.checkEligibility(uid);
        assertFalse(vo.isEligible());
        assertTrue(vo.getReason().contains("发帖"));
    }

    @Test
    @Transactional
    @Rollback
    void eligibility_qualified_shouldPass() {
        Long uid = ensureUser("p2m2-elig-ok@example.com", "p2m2合格", 10);
        createPosts(uid, 3);
        EligibilityVO vo = applicationService.checkEligibility(uid);
        assertTrue(vo.isEligible());
        assertEquals(3, vo.getPostCount());
    }

    @Test
    @Transactional
    @Rollback
    void eligibility_hasPending_shouldBeIneligible() {
        Long uid = ensureUser("p2m2-elig-pending@example.com", "p2m2待审", 10);
        createPosts(uid, 3);
        // 提交一个
        applicationService.submit(uid, validRequest("二期测试吧A"));
        EligibilityVO vo = applicationService.checkEligibility(uid);
        assertFalse(vo.isEligible());
        assertTrue(vo.isHasPending());
    }

    // ===== 提交 =====

    @Test
    @Transactional
    @Rollback
    void submit_qualified_shouldCreatePendingApplication() {
        Long uid = ensureUser("p2m2-submit@example.com", "p2m2提交", 10);
        createPosts(uid, 3);
        Long appId = applicationService.submit(uid, validRequest("二期测试吧B"));
        BoardApplication app = applicationMapper.selectById(appId);
        assertNotNull(app);
        assertEquals(1, app.getStatus().intValue());
        assertEquals("二期测试吧B", app.getName());
    }

    @Test
    @Transactional
    @Rollback
    void submit_nameConflict_shouldThrow() {
        Long uid = ensureUser("p2m2-conflict@example.com", "p2m2冲突", 10);
        createPosts(uid, 3);
        // 一期种子板块"技术交流"是 status=1 唯一占用
        BoardApplicationSubmitRequest r = validRequest("技术交流");
        BizException ex = assertThrows(BizException.class, () -> applicationService.submit(uid, r));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("已被使用"));
    }

    @Test
    @Transactional
    @Rollback
    void submit_tooManyTags_shouldThrow() {
        Long uid = ensureUser("p2m2-tag@example.com", "p2m2标签", 10);
        createPosts(uid, 3);
        BoardApplicationSubmitRequest r = validRequest("二期标签测试");
        r.setTags("a,b,c,d");  // 4 个
        BizException ex = assertThrows(BizException.class, () -> applicationService.submit(uid, r));
        assertTrue(ex.getMessage().contains("标签"));
    }

    // ===== 审核 =====

    @Test
    @Transactional
    @Rollback
    void approve_shouldCreateBoardAndAutoFollow() {
        Long uid = ensureUser("p2m2-approve@example.com", "p2m2通过", 10);
        createPosts(uid, 3);
        Long appId = applicationService.submit(uid, validRequest("二期测试吧C"));
        // 假装系统管理员审批，id=1
        Long boardId = applicationService.approve(1L, appId);

        Board board = boardMapper.selectById(boardId);
        assertNotNull(board);
        assertEquals("二期测试吧C", board.getName());
        assertEquals(uid, board.getOwnerUserId());
        assertEquals(1, board.getFollowerCount().intValue());

        BoardApplication app = applicationMapper.selectById(appId);
        assertEquals(2, app.getStatus().intValue());
        assertEquals(boardId, app.getBoardId());

        UserBoardFollow follow = followMapper.selectByUserAndBoard(uid, boardId);
        assertNotNull(follow, "申请人应被自动加入关注关系");
    }

    @Test
    @Transactional
    @Rollback
    void approve_alreadyReviewed_shouldThrow() {
        Long uid = ensureUser("p2m2-approve2@example.com", "p2m2双通过", 10);
        createPosts(uid, 3);
        Long appId = applicationService.submit(uid, validRequest("二期测试吧D"));
        applicationService.approve(1L, appId);
        BizException ex = assertThrows(BizException.class, () -> applicationService.approve(1L, appId));
        assertTrue(ex.getMessage().contains("状态已变化"));
    }

    @Test
    @Transactional
    @Rollback
    void reject_shouldSetStatus3AndReason() {
        Long uid = ensureUser("p2m2-reject@example.com", "p2m2驳回", 10);
        createPosts(uid, 3);
        Long appId = applicationService.submit(uid, validRequest("二期测试吧E"));
        applicationService.reject(1L, appId, "板块名涉及违规");

        BoardApplication app = applicationMapper.selectById(appId);
        assertEquals(3, app.getStatus().intValue());
        assertEquals("板块名涉及违规", app.getRejectReason());
    }
}
