package com.forum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户-板块关注关系实体（P2-M3）
 *
 * 对应 user_board_follow 表，复合主键 (user_id, board_id) 保证幂等。
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Data
@TableName("user_board_follow")
public class UserBoardFollow implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long boardId;

    private LocalDateTime createdAt;
}
