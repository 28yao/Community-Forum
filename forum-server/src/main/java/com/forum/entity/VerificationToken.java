package com.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 验证令牌实体
 *
 * 对应 verification_token 表（plan.md §3.2.8）。
 * 用于邮箱注册验证、密码重置等一次性令牌场景。
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Data
@TableName("verification_token")
public class VerificationToken implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的用户 ID（注册验证流程刚生成时可能为空） */
    private Long userId;

    /** 邮箱 */
    private String email;

    /** 令牌字符串（全局唯一） */
    private String token;

    /** 令牌类型 */
    private String type;

    /** 是否已使用 (0/1) */
    private Integer used;

    /** 过期时间 */
    private LocalDateTime expiresAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    // ===== 令牌类型常量 =====
    public static final String TYPE_REGISTER = "register";
    public static final String TYPE_RESET_PASSWORD = "reset_password";
}
