package com.forum.service;

import com.forum.common.ErrorCode;
import com.forum.common.exception.BizException;
import com.forum.entity.Post;
import com.forum.entity.PostLike;
import com.forum.mapper.PostLikeMapper;
import com.forum.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 点赞 Service（M4-T2）
 *
 * 幂等保证：DB UNIQUE(post_id,user_id) + 应用层先查后写 + 并发 UNIQUE 兜底
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {

    private final PostLikeMapper likeMapper;
    private final PostMapper postMapper;

    /** 点赞：已点则幂等，未点则插入 + post.like_count++ */
    @Transactional(rollbackFor = Exception.class)
    public boolean like(Long postId, Long userId) {
        Post p = postMapper.selectById(postId);
        if (p == null) {
            throw new BizException(ErrorCode.POST_NOT_FOUND);
        }
        // 幂等：已点则直接返回
        if (likeMapper.selectByPostAndUser(postId, userId) != null) {
            return true;
        }
        try {
            PostLike pl = new PostLike();
            pl.setPostId(postId);
            pl.setUserId(userId);
            likeMapper.insert(pl);
            postMapper.incrLikeCount(postId);
        } catch (DuplicateKeyException e) {
            // 并发兜底
            log.warn("[LIKE] duplicate insert ignored postId={} userId={}", postId, userId);
        }
        return true;
    }

    /** 取消点赞：未点则幂等，已点则删除 + post.like_count-- */
    @Transactional(rollbackFor = Exception.class)
    public boolean unlike(Long postId, Long userId) {
        Post p = postMapper.selectById(postId);
        if (p == null) {
            throw new BizException(ErrorCode.POST_NOT_FOUND);
        }
        int affected = likeMapper.deleteByPostAndUser(postId, userId);
        if (affected > 0) {
            postMapper.decrLikeCount(postId);
        }
        return false;
    }

    public boolean isLiked(Long postId, Long userId) {
        if (postId == null || userId == null) return false;
        return likeMapper.selectByPostAndUser(postId, userId) != null;
    }
}
