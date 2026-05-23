package com.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forum.common.ErrorCode;
import com.forum.common.PageResult;
import com.forum.common.exception.BizException;
import com.forum.entity.Board;
import com.forum.entity.Post;
import com.forum.entity.PostImage;
import com.forum.entity.User;
import com.forum.mapper.BoardMapper;
import com.forum.mapper.PostImageMapper;
import com.forum.mapper.PostMapper;
import com.forum.mapper.UserMapper;
import com.forum.service.dto.CreatePostRequest;
import com.forum.service.dto.UpdatePostRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 帖子业务服务（M3-T7/T9/T11/T13/T15）
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final PostImageMapper postImageMapper;
    private final BoardMapper boardMapper;
    private final UserMapper userMapper;
    private final com.forum.mapper.PostLikeMapper postLikeMapper;
    private final com.forum.mapper.PostFavoriteMapper postFavoriteMapper;

    /** 单帖最多图片数 */
    private static final int MAX_IMAGES = 9;

    /**
     * 发帖（M3-T7）
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(Long userId, CreatePostRequest req) {
        // 1. 版块存在校验
        Board board = boardMapper.selectById(req.getBoardId());
        if (board == null || !Integer.valueOf(1).equals(board.getStatus())) {
            throw new BizException(ErrorCode.BOARD_NOT_FOUND);
        }
        // 2. 图片数量
        List<String> urls = req.getImageUrls();
        if (urls != null && urls.size() > MAX_IMAGES) {
            throw new BizException(ErrorCode.UPLOAD_COUNT_EXCEEDED);
        }
        // 3. 插入帖子
        Post p = new Post();
        p.setBoardId(req.getBoardId());
        p.setUserId(userId);
        p.setTitle(req.getTitle());
        p.setContent(req.getContent());
        p.setIsAnonymous(0);
        p.setIsPinned(0);
        p.setIsFeatured(0);
        p.setLikeCount(0);
        p.setCommentCount(0);
        p.setViewCount(0);
        p.setIsEdited(0);
        p.setStatus(1);
        postMapper.insert(p);
        // 4. 插入图片
        if (urls != null) {
            int order = 0;
            for (String url : urls) {
                PostImage img = new PostImage();
                img.setPostId(p.getId());
                img.setUrl(url);
                img.setSortOrder(order++);
                postImageMapper.insert(img);
            }
        }
        // 5. 版块 post_count++
        boardMapper.incrPostCount(req.getBoardId());
        log.info("[CREATE_POST] user={} post={} board={}", userId, p.getId(), req.getBoardId());
        return p.getId();
    }

    /**
     * 帖子列表（M3-T9）
     *
     * 排序：is_pinned DESC, created_at DESC。仅返回 status=1 且未软删除。
     */
    public PageResult<Map<String, Object>> listPosts(Long boardId, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 50) size = 20;

        LambdaQueryWrapper<Post> w = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, 1)
                .orderByDesc(Post::getIsPinned)
                .orderByDesc(Post::getCreatedAt);
        if (boardId != null) {
            w.eq(Post::getBoardId, boardId);
        }
        Page<Post> p = postMapper.selectPage(new Page<>(page, size), w);

        List<Map<String, Object>> items = p.getRecords().stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
        return PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), items);
    }

    private Map<String, Object> toListItem(Post post) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", post.getId());
        m.put("boardId", post.getBoardId());
        m.put("title", post.getTitle());
        // 摘要：去掉 HTML 标签后截前 100 字
        String plain = post.getContent() == null ? "" : post.getContent().replaceAll("<[^>]+>", "");
        m.put("summary", plain.length() > 100 ? plain.substring(0, 100) + "..." : plain);
        m.put("likeCount", post.getLikeCount());
        m.put("commentCount", post.getCommentCount());
        m.put("viewCount", post.getViewCount());
        m.put("isPinned", post.getIsPinned());
        m.put("isEdited", post.getIsEdited());
        m.put("createdAt", post.getCreatedAt());

        // 首图
        List<PostImage> imgs = postImageMapper.selectByPostId(post.getId());
        m.put("firstImage", imgs.isEmpty() ? null : imgs.get(0).getUrl());
        m.put("imageCount", imgs.size());

        // 作者
        User u = userMapper.selectById(post.getUserId());
        Map<String, Object> author = new HashMap<>();
        if (u != null) {
            author.put("id", u.getId());
            author.put("nickname", u.getNickname());
            author.put("avatar", u.getAvatar());
        } else {
            author.put("id", null);
            author.put("nickname", "已注销");
            author.put("avatar", null);
        }
        m.put("author", author);
        return m;
    }

    /**
     * 详情（M3-T11）。浏览数+1。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> getPostDetail(Long postId) {
        return getPostDetail(postId, null);
    }

    public Map<String, Object> getPostDetail(Long postId, Long currentUserId) {
        Post post = postMapper.selectById(postId);
        if (post == null || !Integer.valueOf(1).equals(post.getStatus())) {
            throw new BizException(ErrorCode.POST_NOT_FOUND);
        }
        postMapper.incrViewCount(postId);

        Map<String, Object> m = new HashMap<>();
        m.put("id", post.getId());
        m.put("boardId", post.getBoardId());
        m.put("title", post.getTitle());
        m.put("content", post.getContent());
        m.put("likeCount", post.getLikeCount());
        m.put("commentCount", post.getCommentCount());
        m.put("viewCount", post.getViewCount() + 1);
        m.put("isPinned", post.getIsPinned());
        m.put("isEdited", post.getIsEdited());
        m.put("lastEditedAt", post.getLastEditedAt());
        m.put("createdAt", post.getCreatedAt());

        List<String> urls = postImageMapper.selectByPostId(postId).stream()
                .map(PostImage::getUrl).collect(Collectors.toList());
        m.put("images", urls);

        Board b = boardMapper.selectById(post.getBoardId());
        Map<String, Object> board = new HashMap<>();
        if (b != null) {
            board.put("id", b.getId());
            board.put("name", b.getName());
        }
        m.put("board", board);

        User u = userMapper.selectById(post.getUserId());
        Map<String, Object> author = new HashMap<>();
        if (u != null) {
            author.put("id", u.getId());
            author.put("nickname", u.getNickname());
            author.put("avatar", u.getAvatar());
            author.put("bio", u.getBio());
        } else {
            author.put("nickname", "已注销");
        }
        m.put("author", author);

        // 当前用户互动状态（未登录时为 false）
        boolean liked = currentUserId != null
                && postLikeMapper.selectByPostAndUser(postId, currentUserId) != null;
        boolean favorited = currentUserId != null
                && postFavoriteMapper.selectByPostAndUser(postId, currentUserId) != null;
        m.put("liked", liked);
        m.put("favorited", favorited);
        return m;
    }

    /**
     * 编辑帖子（M3-T13）。仅作者本人。
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(Long postId, Long userId, UpdatePostRequest req) {
        Post post = mustOwn(postId, userId);

        List<String> urls = req.getImageUrls();
        if (urls != null && urls.size() > MAX_IMAGES) {
            throw new BizException(ErrorCode.UPLOAD_COUNT_EXCEEDED);
        }

        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setIsEdited(1);
        post.setLastEditedAt(LocalDateTime.now());
        postMapper.updateById(post);

        // 简化策略：删除原有图片 → 重新插入
        if (urls != null) {
            postImageMapper.deleteByPostId(postId);
            int order = 0;
            for (String url : urls) {
                PostImage img = new PostImage();
                img.setPostId(postId);
                img.setUrl(url);
                img.setSortOrder(order++);
                postImageMapper.insert(img);
            }
        }
        log.info("[UPDATE_POST] user={} post={}", userId, postId);
    }

    /**
     * 软删除（M3-T15）。仅作者本人或管理员。
     * board.post_count-- ；评论级联标记由 M4 负责自己的 deleted 字段。
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId, Long userId, boolean isAdmin) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BizException(ErrorCode.POST_NOT_FOUND);
        }
        if (!isAdmin && !post.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        postMapper.deleteById(postId);
        boardMapper.decrPostCount(post.getBoardId());
        log.info("[DELETE_POST] user={} post={}", userId, postId);
    }

    /** 鉴权辅助：必须是作者本人 */
    private Post mustOwn(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BizException(ErrorCode.POST_NOT_FOUND);
        }
        if (!post.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return post;
    }
}
