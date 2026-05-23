package com.forum.service;

import com.forum.common.ErrorCode;
import com.forum.common.exception.BizException;
import com.forum.entity.Post;
import com.forum.entity.PostFavorite;
import com.forum.mapper.PostFavoriteMapper;
import com.forum.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 收藏 Service（M4-T16）
 *
 * 幂等保证：DB UNIQUE(post_id,user_id) + 应用层先查后写
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final PostFavoriteMapper favoriteMapper;
    private final PostMapper postMapper;

    @Transactional(rollbackFor = Exception.class)
    public boolean favorite(Long postId, Long userId) {
        Post p = postMapper.selectById(postId);
        if (p == null) {
            throw new BizException(ErrorCode.POST_NOT_FOUND);
        }
        if (favoriteMapper.selectByPostAndUser(postId, userId) != null) {
            return true;
        }
        try {
            PostFavorite pf = new PostFavorite();
            pf.setPostId(postId);
            pf.setUserId(userId);
            favoriteMapper.insert(pf);
        } catch (DuplicateKeyException e) {
            log.warn("[FAVORITE] duplicate insert ignored postId={} userId={}", postId, userId);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean unfavorite(Long postId, Long userId) {
        Post p = postMapper.selectById(postId);
        if (p == null) {
            throw new BizException(ErrorCode.POST_NOT_FOUND);
        }
        favoriteMapper.deleteByPostAndUser(postId, userId);
        return false;
    }

    public boolean isFavorited(Long postId, Long userId) {
        if (postId == null || userId == null) return false;
        return favoriteMapper.selectByPostAndUser(postId, userId) != null;
    }
}
