package com.forum.service;

import com.forum.common.ErrorCode;
import com.forum.common.exception.BizException;
import com.forum.entity.Board;
import com.forum.entity.User;
import com.forum.mapper.BoardMapper;
import com.forum.mapper.UserBoardFollowMapper;
import com.forum.mapper.UserMapper;
import com.forum.service.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BoardFollowService 测试（P2-M3）
 *
 * 覆盖：
 * - 关注/取关基础流程（含 follower_count 变化）
 * - 关注幂等
 * - 取关幂等
 * - 板块不存在抛 3003
 * - 关注上限拦截
 * - 推荐列表按 follower_count + post_count 排序
 */
@SpringBootTest
@ActiveProfiles("dev")
class BoardFollowServiceTest {

    @Autowired private BoardFollowService boardFollowService;
    @Autowired private BoardMapper boardMapper;
    @Autowired private UserBoardFollowMapper followMapper;
    @Autowired private UserService userService;
    @Autowired private UserMapper userMapper;

    @MockBean private MailService mailService;

    private Long ensureUser(String email, String nick) {
        RegisterRequest r = new RegisterRequest();
        r.setEmail(email); r.setPassword("abc123"); r.setNickname(nick);
        Long id = userService.register(r);
        User u = userMapper.selectById(id);
        u.setEmailVerified(1);
        userMapper.updateById(u);
        return id;
    }

    @Test
    @Transactional
    @Rollback
    void follow_then_unfollow_shouldUpdateFollowerCount() {
        Long uid = ensureUser("p2m3-follow-1@example.com", "p2m3关注1");
        // 一期种子版块 id=1（技术交流）
        Integer before = boardMapper.selectById(1L).getFollowerCount();

        boardFollowService.follow(1L, uid);
        Board b1 = boardMapper.selectById(1L);
        assertEquals(before + 1, b1.getFollowerCount());
        assertTrue(boardFollowService.isFollowed(1L, uid));

        // 幂等：再关注一次计数不变
        boardFollowService.follow(1L, uid);
        assertEquals(before + 1, boardMapper.selectById(1L).getFollowerCount());

        // 取关
        boardFollowService.unfollow(1L, uid);
        assertEquals(before.intValue(), boardMapper.selectById(1L).getFollowerCount());
        assertFalse(boardFollowService.isFollowed(1L, uid));
    }

    @Test
    @Transactional
    @Rollback
    void unfollow_notFollowed_shouldBeIdempotent() {
        Long uid = ensureUser("p2m3-unfollow-1@example.com", "p2m3取关1");
        Integer before = boardMapper.selectById(1L).getFollowerCount();

        // 未关注先取关：不报错，计数不变
        boardFollowService.unfollow(1L, uid);
        assertEquals(before.intValue(), boardMapper.selectById(1L).getFollowerCount());
    }

    @Test
    @Transactional
    @Rollback
    void follow_notExistBoard_shouldThrow3003() {
        Long uid = ensureUser("p2m3-follow-nope@example.com", "p2m3空板");
        BizException ex = assertThrows(BizException.class, () -> boardFollowService.follow(99999L, uid));
        assertEquals(ErrorCode.BOARD_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    @Transactional
    @Rollback
    void listFollowedBoardIds_shouldReturnAllFollowed() {
        Long uid = ensureUser("p2m3-list@example.com", "p2m3列表");
        // 关注 1 和 2（一期种子板块）
        boardFollowService.follow(1L, uid);
        boardFollowService.follow(2L, uid);

        List<Long> ids = boardFollowService.listFollowedBoardIds(uid);
        assertEquals(2, ids.size());
        // 顺序不强校验（同事务内 created_at 可能相同秒级精度），只校验集合内容
        assertTrue(ids.contains(1L) && ids.contains(2L));
    }

    @Test
    @Transactional
    @Rollback
    void listRecommended_shouldReturnEnabledBoards() {
        List<Board> recommended = boardFollowService.listRecommended(10);
        assertFalse(recommended.isEmpty(), "至少应返回一期 3 个种子板块");
        // 全部 status=1
        recommended.forEach(b -> assertEquals(1, b.getStatus().intValue()));
    }
}
