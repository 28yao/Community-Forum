package com.forum.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.entity.UserBoardFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户-板块关注 Mapper（P2-M3）
 */
@Mapper
public interface UserBoardFollowMapper extends BaseMapper<UserBoardFollow> {

    /** 查询某用户的全部关注 board_id */
    @Select("SELECT board_id FROM user_board_follow WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Long> selectBoardIdsByUserId(@Param("userId") Long userId);

    /** 统计某用户关注数 */
    default long countByUserId(Long userId) {
        return selectCount(new LambdaQueryWrapper<UserBoardFollow>()
                .eq(UserBoardFollow::getUserId, userId));
    }

    /** 查关注关系是否存在 */
    default UserBoardFollow selectByUserAndBoard(Long userId, Long boardId) {
        return selectOne(new LambdaQueryWrapper<UserBoardFollow>()
                .eq(UserBoardFollow::getUserId, userId)
                .eq(UserBoardFollow::getBoardId, boardId));
    }

    /** 删除一条关注关系 */
    default int deleteByUserAndBoard(Long userId, Long boardId) {
        return delete(new LambdaQueryWrapper<UserBoardFollow>()
                .eq(UserBoardFollow::getUserId, userId)
                .eq(UserBoardFollow::getBoardId, boardId));
    }
}
