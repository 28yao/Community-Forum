package com.forum.service;

import com.forum.common.ErrorCode;
import com.forum.common.exception.BizException;
import com.forum.entity.Post;
import com.forum.entity.User;
import com.forum.mapper.PostMapper;
import com.forum.mapper.UserMapper;
import com.forum.service.dto.CreatePostRequest;
import com.forum.service.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * M4 互动模块综合测试：LikeService / FavoriteService / CommentService
 */
@SpringBootTest
@ActiveProfiles("dev")
class InteractionServiceTest {

    @Autowired private UserService userService;
    @Autowired private PostService postService;
    @Autowired private LikeService likeService;
    @Autowired private FavoriteService favoriteService;
    @Autowired private CommentService commentService;
    @Autowired private UserMapper userMapper;
    @Autowired private PostMapper postMapper;

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

    private Long ensurePost(Long uid) {
        CreatePostRequest r = new CreatePostRequest();
        r.setBoardId(1L); r.setTitle("互动测试帖" + System.nanoTime()); r.setContent("body");
        return postService.createPost(uid, r);
    }

    // ===== Like =====

    @Test
    @Transactional
    @Rollback
    void like_then_unlike_shouldUpdateCount() {
        Long uid = ensureUser("i-like@example.com", "互动点赞");
        Long pid = ensurePost(uid);

        likeService.like(pid, uid);
        Post p1 = postMapper.selectById(pid);
        assertEquals(1, p1.getLikeCount());
        assertTrue(likeService.isLiked(pid, uid));

        // 幂等：再点一次还是 1
        likeService.like(pid, uid);
        assertEquals(1, postMapper.selectById(pid).getLikeCount());

        // 取消
        likeService.unlike(pid, uid);
        assertEquals(0, postMapper.selectById(pid).getLikeCount());
        assertFalse(likeService.isLiked(pid, uid));
    }

    @Test
    @Transactional
    @Rollback
    void like_notExistPost_shouldThrow3001() {
        Long uid = ensureUser("i-like-nope@example.com", "点赞空");
        BizException ex = assertThrows(BizException.class, () -> likeService.like(99999999L, uid));
        assertEquals(ErrorCode.POST_NOT_FOUND.getCode(), ex.getCode());
    }

    // ===== Favorite =====

    @Test
    @Transactional
    @Rollback
    void favorite_idempotent() {
        Long uid = ensureUser("i-fav@example.com", "互动收藏");
        Long pid = ensurePost(uid);

        favoriteService.favorite(pid, uid);
        assertTrue(favoriteService.isFavorited(pid, uid));
        favoriteService.favorite(pid, uid); // 幂等
        assertTrue(favoriteService.isFavorited(pid, uid));

        favoriteService.unfavorite(pid, uid);
        assertFalse(favoriteService.isFavorited(pid, uid));
    }

    // ===== Comment =====

    @Test
    @Transactional
    @Rollback
    void createComment_top_then_child_shouldBuildTree() {
        Long uid = ensureUser("i-cmt-a@example.com", "评论作者");
        Long uid2 = ensureUser("i-cmt-b@example.com", "评论回复者");
        Long pid = ensurePost(uid);

        Long c1 = commentService.createComment(pid, uid, "顶级评论", null, null);
        Long c2 = commentService.createComment(pid, uid2, "回复 c1", c1, null);
        // 对子级评论再回复 → parent 应当指向 c1（顶级）
        Long c3 = commentService.createComment(pid, uid, "再回复 c2", c2, null);

        List<Map<String, Object>> tree = commentService.listComments(pid);
        assertEquals(1, tree.size());
        Map<String, Object> top = tree.get(0);
        assertEquals(c1, top.get("id"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> children = (List<Map<String, Object>>) top.get("children");
        assertEquals(2, children.size(), "对 c2 的回复应当挂在 c1 下");

        // 评论数 +3
        assertEquals(3, postMapper.selectById(pid).getCommentCount());
    }

    @Test
    @Transactional
    @Rollback
    void createComment_emptyContent_shouldThrow() {
        Long uid = ensureUser("i-cmt-empty@example.com", "评论空");
        Long pid = ensurePost(uid);
        BizException ex = assertThrows(BizException.class,
                () -> commentService.createComment(pid, uid, "   ", null, null));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
    }

    @Test
    @Transactional
    @Rollback
    void createComment_parentOnOtherPost_shouldThrow() {
        Long uid = ensureUser("i-cmt-mp@example.com", "跨帖父");
        Long p1 = ensurePost(uid);
        Long p2 = ensurePost(uid);
        Long parent = commentService.createComment(p1, uid, "p1 顶级", null, null);
        BizException ex = assertThrows(BizException.class,
                () -> commentService.createComment(p2, uid, "x", parent, null));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
    }

    @Test
    @Transactional
    @Rollback
    void deleteComment_byOther_shouldThrow1002() {
        Long author = ensureUser("i-cmt-da@example.com", "评论作者d");
        Long other = ensureUser("i-cmt-do@example.com", "评论他人d");
        Long pid = ensurePost(author);
        Long cid = commentService.createComment(pid, author, "x", null, null);

        BizException ex = assertThrows(BizException.class,
                () -> commentService.deleteComment(cid, other, false));
        assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
    }

    @Test
    @Transactional
    @Rollback
    void deleteComment_byAuthor_shouldSoftDeleteAndDecr() {
        Long uid = ensureUser("i-cmt-d@example.com", "评论删除");
        Long pid = ensurePost(uid);
        Long cid = commentService.createComment(pid, uid, "x", null, null);
        int before = postMapper.selectById(pid).getCommentCount();
        commentService.deleteComment(cid, uid, false);
        assertEquals(before - 1, postMapper.selectById(pid).getCommentCount());
        // 列表应当过滤
        assertTrue(commentService.listComments(pid).stream()
                .noneMatch(m -> m.get("id").equals(cid)));
    }
}
