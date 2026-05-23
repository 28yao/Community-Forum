package com.forum.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 点赞 Mapper
 */
@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {

    default PostLike selectByPostAndUser(Long postId, Long userId) {
        return selectOne(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId));
    }

    default int deleteByPostAndUser(Long postId, Long userId) {
        return delete(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId));
    }
}
