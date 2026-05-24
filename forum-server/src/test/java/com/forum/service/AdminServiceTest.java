package com.forum.service;

import com.forum.common.ErrorCode;
import com.forum.common.PageResult;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * M6 后台管理服务测试：UserService ban/unban/resetPassword + PostService 恢复/置顶 + CommentService 后台
 */
@SpringBootTest
@ActiveProfiles("dev")
class AdminServiceTest {

    @Autowired private UserService userService;
    @Autowired private PostService postService;
    @Autowired private CommentService commentService;
    @Autowired private BoardService boardService;
    @Autowired private UserMapper userMapper;
    @Autowired private PostMapper postMapper;
    @Autowired private RedisTemplate<String, Object> redisTemplate;
    @MockBean private MailService mailService;

    private Long admin() {
        // 复用种子 admin@forum.com（id=1）
        User u = userMapper.selectByEmail("admin@forum.com");
        return u != null ? u.getId() : 1L;
    }

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
    void banUser_shouldUpdateStatusAndClearToken() {
        Long opId = admin();
        Long uid = ensureUser("a-ban@example.com", "封禁测试");
        // 模拟该用户已登录：Redis 中有 token
        redisTemplate.opsForValue().set("token:" + uid, "fake-token-xxx");

        userService.banUser(uid, opId, "测试封禁");

        User after = userMapper.selectById(uid);
        assertEquals(0, after.getStatus());
        assertEquals("测试封禁", after.getBanReason());
        assertNull(redisTemplate.opsForValue().get("token:" + uid), "封禁后 Token 应被清除");
    }

    @Test
    void banUser_self_shouldThrow() {
        Long opId = admin();
        BizException ex = assertThrows(BizException.class,
                () -> userService.banUser(opId, opId, "自封"));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("自己"));
    }

    @Test
    @Transactional
    @Rollback
    void banUser_admin_shouldThrow() {
        Long opId = admin();
        // 制造另一个 admin
        Long other = ensureUser("a-other-admin@example.com", "另管理员" + System.nanoTime());
        User u = userMapper.selectById(other);
        u.setRole("admin");
        userMapper.updateById(u);

        BizException ex = assertThrows(BizException.class,
                () -> userService.banUser(other, opId, "封管理员"));
        assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
    }

    @Test
    @Transactional
    @Rollback
    void resetPassword_shouldClearToken() {
        Long opId = admin();
        Long uid = ensureUser("a-reset@example.com", "重置密码");
        redisTemplate.opsForValue().set("token:" + uid, "fake-token");

        userService.adminResetPassword(uid, opId, "newpass456");

        assertNull(redisTemplate.opsForValue().get("token:" + uid));
        // 老密码不能用，新密码可以用（密码已经哈希存储，直接登录验证）
        com.forum.service.dto.LoginRequest req = new com.forum.service.dto.LoginRequest();
        req.setEmail("a-reset@example.com");
        req.setPassword("newpass456");
        Map<String, Object> result = userService.login(req);
        assertNotNull(result.get("token"));
    }

    @Test
    void resetPassword_tooShort_shouldThrow() {
        Long opId = admin();
        BizException ex = assertThrows(BizException.class,
                () -> userService.adminResetPassword(opId, opId, "x"));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
    }

    @Test
    @Transactional
    @Rollback
    void adminListUsers_shouldPaginate() {
        PageResult<Map<String, Object>> result = userService.adminListUsers(null, 1, 5);
        assertNotNull(result);
        assertTrue(result.getTotal() >= 1, "至少应有 admin");
        assertTrue(result.getList().size() <= 5);
    }

    @Test
    void postRestore_softDeleted_shouldRecover() {
        Long opId = admin();
        Long uid = ensureUser("a-restore-" + System.nanoTime() + "@example.com", "恢复测试" + System.nanoTime());
        CreatePostRequest r = new CreatePostRequest();
        r.setBoardId(1L); r.setTitle("待恢复" + System.nanoTime()); r.setContent("body");
        Long pid = postService.createPost(uid, r);
        try {
            postService.deletePost(pid, uid, false);
            assertNull(postMapper.selectById(pid)); // 已软删

            postService.restorePost(pid, opId);
            assertNotNull(postMapper.selectById(pid)); // 恢复
        } finally {
            // 清理
            postService.deletePost(pid, opId, true);
            userMapper.deleteById(uid);
        }
    }

    @Test
    @Transactional
    @Rollback
    void pinPost_shouldToggle() {
        Long opId = admin();
        Long uid = ensureUser("a-pin@example.com", "置顶测试");
        CreatePostRequest r = new CreatePostRequest();
        r.setBoardId(1L); r.setTitle("待置顶"); r.setContent("body");
        Long pid = postService.createPost(uid, r);

        postService.pinPost(pid, true, opId, true);
        assertEquals(1, postMapper.selectById(pid).getIsPinned());
        postService.pinPost(pid, false, opId, true);
        assertEquals(0, postMapper.selectById(pid).getIsPinned());
    }

    @Test
    @Transactional
    @Rollback
    void adminListPosts_shouldIncludeDeleted() {
        Long uid = ensureUser("a-list@example.com", "后台列表");
        CreatePostRequest r = new CreatePostRequest();
        r.setBoardId(1L); r.setTitle("后台帖" + System.nanoTime()); r.setContent("body");
        Long pid = postService.createPost(uid, r);
        postService.deletePost(pid, uid, false);

        PageResult<Map<String, Object>> result = postService.adminListPosts(1L, null, 1, 100);
        // 软删的应该出现
        assertTrue(result.getList().stream().anyMatch(m -> pid.equals(m.get("id"))));
    }

    @Test
    @Transactional
    @Rollback
    void createBoard_duplicateName_shouldThrow() {
        Long opId = admin();
        String name = "测试版块" + System.nanoTime();
        Long id1 = boardService.createBoard(name, "描述", 0, opId);
        assertNotNull(id1);
        BizException ex = assertThrows(BizException.class,
                () -> boardService.createBoard(name, "", 0, opId));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
    }

    @Test
    @Transactional
    @Rollback
    void setBoardStatus_shouldToggleVisibility() {
        Long opId = admin();
        String name = "禁用测试版块" + System.nanoTime();
        Long id = boardService.createBoard(name, "", 0, opId);
        boardService.setBoardStatus(id, 0, opId);
        // 列表中不应出现禁用版块
        assertTrue(boardService.listEnabled().stream().noneMatch(b -> b.getId().equals(id)));
        // 后台列表应包含
        assertTrue(boardService.adminListAll().stream().anyMatch(b -> b.getId().equals(id)));
    }
}
