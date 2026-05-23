package com.forum.service;

import com.forum.common.ErrorCode;
import com.forum.common.PageResult;
import com.forum.common.exception.BizException;
import com.forum.entity.Post;
import com.forum.entity.PostImage;
import com.forum.mapper.PostImageMapper;
import com.forum.mapper.PostMapper;
import com.forum.mapper.UserMapper;
import com.forum.service.dto.CreatePostRequest;
import com.forum.service.dto.RegisterRequest;
import com.forum.service.dto.UpdatePostRequest;
import com.forum.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PostService 测试（M3-T7/T9/T11/T13/T15）
 */
@SpringBootTest
@ActiveProfiles("dev")
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostImageMapper postImageMapper;

    @Autowired
    private UserMapper userMapper;

    @MockBean
    private MailService mailService;

    private Long ensureUser(String email, String nickname) {
        RegisterRequest r = new RegisterRequest();
        r.setEmail(email);
        r.setPassword("abc123");
        r.setNickname(nickname);
        Long id = userService.register(r);
        User u = userMapper.selectById(id);
        u.setEmailVerified(1);
        userMapper.updateById(u);
        return id;
    }

    private CreatePostRequest req(Long boardId, String title, String content, List<String> imgs) {
        CreatePostRequest r = new CreatePostRequest();
        r.setBoardId(boardId);
        r.setTitle(title);
        r.setContent(content);
        r.setImageUrls(imgs);
        return r;
    }

    @Test
    @Transactional
    @Rollback
    void createPost_happyPath_shouldInsertPostAndImages() {
        Long uid = ensureUser("p-ok@example.com", "发帖正常");
        Long pid = postService.createPost(uid, req(1L, "标题A", "<p>内容</p>",
                Arrays.asList("/static/uploads/post/a.jpg", "/static/uploads/post/b.jpg")));
        assertNotNull(pid);
        Post p = postMapper.selectById(pid);
        assertEquals("标题A", p.getTitle());
        assertEquals(uid, p.getUserId());

        List<PostImage> imgs = postImageMapper.selectByPostId(pid);
        assertEquals(2, imgs.size());
        assertEquals(0, imgs.get(0).getSortOrder());
        assertEquals(1, imgs.get(1).getSortOrder());
    }

    @Test
    @Transactional
    @Rollback
    void createPost_invalidBoard_shouldThrow3003() {
        Long uid = ensureUser("p-noboard@example.com", "无版块");
        BizException ex = assertThrows(BizException.class,
                () -> postService.createPost(uid, req(99999L, "标题", "内容", null)));
        assertEquals(ErrorCode.BOARD_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    @Transactional
    @Rollback
    void createPost_tooManyImages_shouldThrow4003() {
        Long uid = ensureUser("p-toomany@example.com", "图过多");
        List<String> imgs = Arrays.asList(
                "/u/1.jpg", "/u/2.jpg", "/u/3.jpg", "/u/4.jpg", "/u/5.jpg",
                "/u/6.jpg", "/u/7.jpg", "/u/8.jpg", "/u/9.jpg", "/u/10.jpg");
        BizException ex = assertThrows(BizException.class,
                () -> postService.createPost(uid, req(1L, "标题", "内容", imgs)));
        assertEquals(ErrorCode.UPLOAD_COUNT_EXCEEDED.getCode(), ex.getCode());
    }

    @Test
    @Transactional
    @Rollback
    void listPosts_shouldReturnPagedAndPinnedFirst() {
        Long uid = ensureUser("p-list@example.com", "列表测试");
        Long p1 = postService.createPost(uid, req(1L, "普通帖A", "内容", null));
        Long p2 = postService.createPost(uid, req(1L, "置顶帖", "内容", null));
        // 把 p2 置顶
        Post p = postMapper.selectById(p2);
        p.setIsPinned(1);
        postMapper.updateById(p);

        PageResult<Map<String, Object>> result = postService.listPosts(1L, 1, 20);
        assertTrue(result.getTotal() >= 2);
        // 置顶应该在第一
        assertEquals(p2, result.getList().get(0).get("id"));
    }

    @Test
    @Transactional
    @Rollback
    void getPostDetail_shouldIncrViewCount() {
        Long uid = ensureUser("p-detail@example.com", "详情测试");
        Long pid = postService.createPost(uid, req(1L, "详情标题", "<p>正文</p>",
                Arrays.asList("/u/x.jpg")));

        Map<String, Object> d = postService.getPostDetail(pid);
        assertEquals("详情标题", d.get("title"));
        @SuppressWarnings("unchecked")
        List<String> images = (List<String>) d.get("images");
        assertEquals(1, images.size());
        // viewCount 应当 +1
        Post after = postMapper.selectById(pid);
        assertEquals(1, after.getViewCount());
    }

    @Test
    void getPostDetail_notExist_shouldThrow3001() {
        BizException ex = assertThrows(BizException.class,
                () -> postService.getPostDetail(99999999L));
        assertEquals(ErrorCode.POST_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    @Transactional
    @Rollback
    void updatePost_byAuthor_shouldMarkEdited() {
        Long uid = ensureUser("p-edit@example.com", "编辑测试");
        Long pid = postService.createPost(uid, req(1L, "原标题", "原内容", null));

        UpdatePostRequest up = new UpdatePostRequest();
        up.setTitle("新标题");
        up.setContent("新内容");
        postService.updatePost(pid, uid, up);

        Post after = postMapper.selectById(pid);
        assertEquals("新标题", after.getTitle());
        assertEquals(1, after.getIsEdited());
        assertNotNull(after.getLastEditedAt());
    }

    @Test
    @Transactional
    @Rollback
    void updatePost_byOther_shouldThrow1002() {
        Long author = ensureUser("p-author@example.com", "作者");
        Long other = ensureUser("p-other@example.com", "他人");
        Long pid = postService.createPost(author, req(1L, "标题", "内容", null));

        UpdatePostRequest up = new UpdatePostRequest();
        up.setTitle("被改的标题");
        up.setContent("被改的内容");
        BizException ex = assertThrows(BizException.class,
                () -> postService.updatePost(pid, other, up));
        assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
    }

    @Test
    @Transactional
    @Rollback
    void deletePost_byAuthor_shouldSoftDelete() {
        Long uid = ensureUser("p-del@example.com", "删除测试");
        Long pid = postService.createPost(uid, req(1L, "待删", "内容", null));

        postService.deletePost(pid, uid, false);
        // selectById 因软删除会过滤
        assertNull(postMapper.selectById(pid));
    }

    @Test
    @Transactional
    @Rollback
    void deletePost_byOther_shouldThrow1002() {
        Long author = ensureUser("p-del-a@example.com", "删除作者");
        Long other = ensureUser("p-del-o@example.com", "删除他人");
        Long pid = postService.createPost(author, req(1L, "标题", "内容", null));

        BizException ex = assertThrows(BizException.class,
                () -> postService.deletePost(pid, other, false));
        assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
    }
}
