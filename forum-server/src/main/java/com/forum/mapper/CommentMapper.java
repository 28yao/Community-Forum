package com.forum.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 评论 Mapper
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /** 查询某帖下所有评论（按创建时间升序，自动过滤已软删除） */
    default List<Comment> selectByPostId(Long postId) {
        return selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId, postId)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreatedAt));
    }
}
