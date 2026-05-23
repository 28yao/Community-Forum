package com.forum.service;

import com.forum.common.ErrorCode;
import com.forum.common.exception.BizException;
import com.forum.entity.Comment;
import com.forum.entity.Post;
import com.forum.entity.User;
import com.forum.mapper.CommentMapper;
import com.forum.mapper.PostMapper;
import com.forum.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论 Service（M4-T6/T7/T8）
 *
 * 层级限制：最多 2 层（顶级 + 子级）；对子级评论再回复时 parentId 自动指向顶级。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    private static final int MAX_DEPTH = 2;
    private static final int MAX_CONTENT_LENGTH = 1000;

    /**
     * 发表评论
     *
     * @param replyToUserId 仅 depth=2 时使用（被回复用户）
     * @return 评论 id
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(Long postId, Long userId, String content, Long parentId, Long replyToUserId) {
        if (content == null || content.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "评论内容不能为空");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new BizException(ErrorCode.PARAM_INVALID, "评论内容不能超过 1000 字");
        }
        Post p = postMapper.selectById(postId);
        if (p == null) {
            throw new BizException(ErrorCode.POST_NOT_FOUND);
        }

        int depth = 1;
        Long finalParentId = null;
        Long finalReplyToUserId = null;

        if (parentId != null) {
            Comment parent = commentMapper.selectById(parentId);
            if (parent == null) {
                throw new BizException(ErrorCode.COMMENT_NOT_FOUND);
            }
            if (!parent.getPostId().equals(postId)) {
                throw new BizException(ErrorCode.PARAM_INVALID, "父评论不属于该帖子");
            }
            if (parent.getDepth() >= MAX_DEPTH) {
                // 对 depth=2 的评论回复时，parent 升级到该评论的祖父（顶级）
                finalParentId = parent.getParentId();
                finalReplyToUserId = parent.getUserId();
                depth = 2;
            } else {
                finalParentId = parent.getId();
                finalReplyToUserId = replyToUserId != null ? replyToUserId : parent.getUserId();
                depth = 2;
            }
        }

        Comment c = new Comment();
        c.setPostId(postId);
        c.setUserId(userId);
        c.setParentId(finalParentId);
        c.setReplyToUserId(finalReplyToUserId);
        c.setContent(content.trim());
        c.setDepth(depth);
        c.setStatus(1);
        commentMapper.insert(c);

        postMapper.incrCommentCount(postId);
        log.info("[COMMENT_CREATE] post={} user={} commentId={} depth={}", postId, userId, c.getId(), depth);
        return c.getId();
    }

    /**
     * 查询某帖的评论树（应用层组装）
     */
    public List<Map<String, Object>> listComments(Long postId) {
        Post p = postMapper.selectById(postId);
        if (p == null) {
            throw new BizException(ErrorCode.POST_NOT_FOUND);
        }
        List<Comment> all = commentMapper.selectByPostId(postId);
        if (all.isEmpty()) return java.util.Collections.emptyList();

        // 批量查询用户
        List<Long> userIds = all.stream().map(Comment::getUserId).distinct().collect(Collectors.toList());
        all.stream().map(Comment::getReplyToUserId).filter(java.util.Objects::nonNull).forEach(userIds::add);
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds.stream().distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(User::getId, x -> x));

        // 拆分顶级 / 子级
        List<Comment> topLevel = all.stream().filter(c -> c.getParentId() == null).collect(Collectors.toList());
        Map<Long, List<Comment>> childrenMap = all.stream().filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(Comment::getParentId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Comment top : topLevel) {
            Map<String, Object> node = toVO(top, userMap);
            List<Map<String, Object>> children = childrenMap.getOrDefault(top.getId(), java.util.Collections.emptyList())
                    .stream().map(c -> toVO(c, userMap)).collect(Collectors.toList());
            node.put("children", children);
            result.add(node);
        }
        return result;
    }

    private Map<String, Object> toVO(Comment c, Map<Long, User> userMap) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("postId", c.getPostId());
        m.put("parentId", c.getParentId());
        m.put("content", c.getContent());
        m.put("depth", c.getDepth());
        m.put("createdAt", c.getCreatedAt());

        User author = userMap.get(c.getUserId());
        Map<String, Object> authorVO = new HashMap<>();
        authorVO.put("id", c.getUserId());
        authorVO.put("nickname", author != null ? author.getNickname() : "已注销");
        authorVO.put("avatar", author != null ? author.getAvatar() : null);
        m.put("author", authorVO);

        if (c.getReplyToUserId() != null) {
            User to = userMap.get(c.getReplyToUserId());
            Map<String, Object> replyTo = new HashMap<>();
            replyTo.put("id", c.getReplyToUserId());
            replyTo.put("nickname", to != null ? to.getNickname() : "已注销");
            m.put("replyTo", replyTo);
        }
        return m;
    }

    /** 软删除评论：作者或 admin */
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId, boolean isAdmin) {
        Comment c = commentMapper.selectById(commentId);
        if (c == null) {
            throw new BizException(ErrorCode.COMMENT_NOT_FOUND);
        }
        if (!isAdmin && !c.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "只能删除自己的评论");
        }
        commentMapper.deleteById(commentId);
        postMapper.decrCommentCount(c.getPostId());
        log.info("[COMMENT_DELETE] commentId={} by user={}", commentId, userId);
    }
}
