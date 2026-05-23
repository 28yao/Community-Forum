package com.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 帖子 Mapper
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /** 浏览数 +1 */
    @Update("UPDATE post SET view_count = view_count + 1 WHERE id = #{id} AND deleted = 0")
    int incrViewCount(@Param("id") Long id);

    /** 点赞数 +1 */
    @Update("UPDATE post SET like_count = like_count + 1 WHERE id = #{id} AND deleted = 0")
    int incrLikeCount(@Param("id") Long id);

    /** 点赞数 -1（不低于 0） */
    @Update("UPDATE post SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{id} AND deleted = 0")
    int decrLikeCount(@Param("id") Long id);

    /** 评论数 +1 */
    @Update("UPDATE post SET comment_count = comment_count + 1 WHERE id = #{id} AND deleted = 0")
    int incrCommentCount(@Param("id") Long id);

    /** 评论数 -1（不低于 0） */
    @Update("UPDATE post SET comment_count = GREATEST(comment_count - 1, 0) WHERE id = #{id} AND deleted = 0")
    int decrCommentCount(@Param("id") Long id);
}
