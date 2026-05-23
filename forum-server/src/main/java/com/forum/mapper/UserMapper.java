package com.forum.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户 Mapper
 *
 * 大部分 CRUD 由 BaseMapper 提供，这里仅扩展业务相关查询。
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /** 按邮箱查询（默认会过滤 deleted=1） */
    default User selectByEmail(String email) {
        return selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
    }

    /** 按昵称查询（默认会过滤 deleted=1） */
    default User selectByNickname(String nickname) {
        return selectOne(new LambdaQueryWrapper<User>().eq(User::getNickname, nickname));
    }

    /**
     * 原子递增登录失败次数
     * @return 影响行数
     */
    @Update("UPDATE `user` SET login_fail_count = login_fail_count + 1 WHERE id = #{id}")
    int incrLoginFailCount(@Param("id") Long id);

    /** 重置登录失败次数为 0 */
    @Update("UPDATE `user` SET login_fail_count = 0, locked_until = NULL WHERE id = #{id}")
    int resetLoginFailCount(@Param("id") Long id);

    /** 设置锁定截止时间 */
    @Update("UPDATE `user` SET locked_until = #{lockedUntil} WHERE id = #{id}")
    int lockAccount(@Param("id") Long id, @Param("lockedUntil") java.time.LocalDateTime lockedUntil);
}
