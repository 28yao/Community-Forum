package com.forum.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forum.entity.BoardApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 板块申请 Mapper（P2-M2）
 */
@Mapper
public interface BoardApplicationMapper extends BaseMapper<BoardApplication> {

    /** 我的申请列表（分页，按创建时间倒序） */
    default IPage<BoardApplication> selectMine(Long userId, long pageNo, long pageSize) {
        return selectPage(new Page<>(pageNo, pageSize),
                new LambdaQueryWrapper<BoardApplication>()
                        .eq(BoardApplication::getApplicantUserId, userId)
                        .orderByDesc(BoardApplication::getCreatedAt));
    }

    /** 审核列表（管理端）：按状态过滤，待审核优先。status=null 返回全部 */
    default IPage<BoardApplication> selectForReview(Integer status, long pageNo, long pageSize) {
        LambdaQueryWrapper<BoardApplication> w = new LambdaQueryWrapper<BoardApplication>()
                .orderByAsc(BoardApplication::getCreatedAt);
        if (status != null) w.eq(BoardApplication::getStatus, status);
        return selectPage(new Page<>(pageNo, pageSize), w);
    }

    /** 统计某用户的待审核数（用于"同时只能 1 个 pending"校验） */
    default long countPendingByUser(Long userId) {
        return selectCount(new LambdaQueryWrapper<BoardApplication>()
                .eq(BoardApplication::getApplicantUserId, userId)
                .eq(BoardApplication::getStatus, 1));
    }

    /** SELECT FOR UPDATE 锁单条（审核时事务内防并发） */
    @Select("SELECT * FROM board_application WHERE id = #{id} FOR UPDATE")
    BoardApplication selectByIdForUpdate(@Param("id") Long id);
}
