package com.forum.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.entity.Board;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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
}
