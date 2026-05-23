package com.forum.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forum.entity.Board;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 版块 Mapper
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Mapper
public interface BoardMapper extends BaseMapper<Board> {

    /** 查询所有启用版块（按 sort_weight DESC） */
    default List<Board> selectEnabledList() {
        return selectList(new LambdaQueryWrapper<Board>()
                .eq(Board::getStatus, 1)
                .orderByDesc(Board::getSortWeight)
                .orderByAsc(Board::getId));
    }

    /** post_count + 1 */
    @Update("UPDATE board SET post_count = post_count + 1 WHERE id = #{id}")
    int incrPostCount(@Param("id") Long id);

    /** post_count - 1（不低于 0） */
    @Update("UPDATE board SET post_count = GREATEST(post_count - 1, 0) WHERE id = #{id}")
    int decrPostCount(@Param("id") Long id);

    /** follower_count + 1（P2-M3） */
    @Update("UPDATE board SET follower_count = follower_count + 1 WHERE id = #{id}")
    int incrFollowerCount(@Param("id") Long id);

    /** follower_count - 1（不低于 0，P2-M3） */
    @Update("UPDATE board SET follower_count = GREATEST(follower_count - 1, 0) WHERE id = #{id}")
    int decrFollowerCount(@Param("id") Long id);

    /** 板块搜索（P2-M7）：LIKE 匹配 name/description/slogan/tags，仅启用状态 */
    @Select("SELECT * FROM board WHERE status = 1 " +
            "AND (name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%') " +
            "OR slogan LIKE CONCAT('%', #{keyword}, '%') " +
            "OR tags LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY follower_count DESC, post_count DESC")
    IPage<Board> searchByKeyword(IPage<Board> page, @Param("keyword") String keyword);

    /** 移交吧主（P2-M8） */
    @Update("UPDATE board SET owner_user_id = #{newOwnerId} WHERE id = #{id}")
    int updateOwner(@Param("id") Long id, @Param("newOwnerId") Long newOwnerId);
}
