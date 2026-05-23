package com.forum.service;

import com.forum.common.ErrorCode;
import com.forum.common.PageResult;
import com.forum.common.exception.BizException;
import com.forum.entity.User;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SearchService 测试（M5-T3）
 */
@SpringBootTest
@ActiveProfiles("dev")
class SearchServiceTest {

    @Autowired private SearchService searchService;
    @Autowired private UserService userService;
    @Autowired private PostService postService;
    @Autowired private UserMapper userMapper;
    @MockBean private MailService mailService;

    private Long ensureUser(String email, String nick) {
        RegisterRequest r = new RegisterRequest();
        r.setEmail(email);
        r.setPassword("abc123");
        r.setNickname(nick);
        Long id = userService.register(r);
        User u = userMapper.selectById(id);
        u.setEmailVerified(1);
        userMapper.updateById(u);
        return id;
    }

    private Long createPost(Long uid, String title, String content) {
        CreatePostRequest r = new CreatePostRequest();
        r.setBoardId(1L);
        r.setTitle(title);
        r.setContent(content);
        return postService.createPost(uid, r);
    }

    @Test
    void search_emptyKeyword_shouldThrow() {
        BizException ex = assertThrows(BizException.class, () -> searchService.search("", 1, 20));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
    }

    @Test
    void search_tooShortKeyword_shouldThrow() {
        BizException ex = assertThrows(BizException.class, () -> searchService.search("a", 1, 20));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("2 个字符"));
    }

    @Test
    void search_hitInTitle_shouldReturn() {
        // FULLTEXT 索引对未提交事务里的 INSERT 不可见，因此本测试不能使用 @Transactional+@Rollback
        // 改为：用唯一关键词插入 → 搜索 → 手工清理
        String tag = "SrchTag" + System.currentTimeMillis();
        Long uid = ensureUser("s-hit-" + System.nanoTime() + "@example.com", "搜索测试" + System.nanoTime());
        Long pid = createPost(uid, tag + " 框架入门教程", "<p>这是一篇关于 " + tag + " Boot 的文章</p>");

        try {
            PageResult<Map<String, Object>> result = searchService.search(tag, 1, 20);
            assertTrue(result.getTotal() >= 1, "应命中含有 tag 的帖子");
            assertTrue(result.getList().stream().anyMatch(m -> pid.equals(m.get("id"))),
                    "结果中应包含刚插入的帖子");
        } finally {
            // 清理
            postService.deletePost(pid, uid, true);
            userMapper.deleteById(uid);
        }
    }

    @Test
    void search_unknownKeyword_shouldEmpty() {
        PageResult<Map<String, Object>> result = searchService.search("xkjasdkjabcdefxyz999", 1, 20);
        assertEquals(0, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }

    @Test
    void search_sqlInjection_shouldNotThrow() {
        // BOOLEAN MODE 保留字会被转义；这里不应抛 500
        PageResult<Map<String, Object>> result = searchService.search("'); DROP TABLE post; --", 1, 20);
        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }
}
