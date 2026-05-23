package com.forum.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.entity.PostFavorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏 Mapper
 */
@Mapper
public interface PostFavoriteMapper extends BaseMapper<PostFavorite> {

    default PostFavorite selectByPostAndUser(Long postId, Long userId) {
        return selectOne(new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getPostId, postId)
                .eq(PostFavorite::getUserId, userId));
    }

    default int deleteByPostAndUser(Long postId, Long userId) {
        return delete(new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getPostId, postId)
                .eq(PostFavorite::getUserId, userId));
    }
}
