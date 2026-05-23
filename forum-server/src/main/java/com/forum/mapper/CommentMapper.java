package com.forum.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forum.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    /** 后台分页（含已软删除） */
    @Select({"<script>",
            "SELECT * FROM comment WHERE 1=1",
            "<if test='postId != null'> AND post_id = #{postId} </if>",
            "ORDER BY created_at DESC",
            "</script>"})
    IPage<Comment> adminListComments(IPage<Comment> page, @Param("postId") Long postId);

    /** 后台按 id 查询（含已删除） */
    @Select("SELECT * FROM comment WHERE id = #{id}")
    Comment selectByIdIncludeDeleted(@Param("id") Long id);

    /** 恢复软删除 */
    @Update("UPDATE comment SET deleted = 0 WHERE id = #{id}")
    int restore(@Param("id") Long id);
}
