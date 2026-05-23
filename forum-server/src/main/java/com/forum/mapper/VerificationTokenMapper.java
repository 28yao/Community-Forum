package com.forum.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.entity.VerificationToken;
import org.apache.ibatis.annotations.Mapper;

/**
 * 验证令牌 Mapper
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Mapper
public interface VerificationTokenMapper extends BaseMapper<VerificationToken> {

    /** 按 token 字符串查询 */
    default VerificationToken selectByToken(String token) {
        return selectOne(new LambdaQueryWrapper<VerificationToken>()
                .eq(VerificationToken::getToken, token));
    }
}
