package com.forum.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.entity.PostImage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 帖子图片 Mapper
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Mapper
public interface PostImageMapper extends BaseMapper<PostImage> {

    default List<PostImage> selectByPostId(Long postId) {
        return selectList(new LambdaQueryWrapper<PostImage>()
                .eq(PostImage::getPostId, postId)
                .orderByAsc(PostImage::getSortOrder)
                .orderByAsc(PostImage::getId));
    }

    default int deleteByPostId(Long postId) {
        return delete(new LambdaQueryWrapper<PostImage>().eq(PostImage::getPostId, postId));
    }
}
