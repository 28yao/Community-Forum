package com.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forum.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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

    /**
     * 关键词全文搜索（M5-T2）
     *
     * 使用 ngram 分词器的 FULLTEXT 索引 idx_search(title, content)；
     * MATCH AGAINST 在 BOOLEAN 模式下兼容短关键词（默认 NATURAL 模式对 <ngram_token_size 的词会返回空）。
     * 关键词由 Service 层做转义，传到这里时已经是安全的字面量。
     */
    @Select("SELECT * FROM post " +
            "WHERE deleted = 0 AND status = 1 " +
            "AND MATCH(title, content) AGAINST(#{keyword} IN BOOLEAN MODE) " +
            "ORDER BY MATCH(title, content) AGAINST(#{keyword} IN BOOLEAN MODE) DESC, created_at DESC")
    IPage<Post> searchByKeyword(IPage<Post> page, @Param("keyword") String keyword);
}
