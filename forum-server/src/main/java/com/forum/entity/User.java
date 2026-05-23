package com.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * 对应 user 表（plan.md §3.2.1）。
 * 字段命名：数据库下划线 → Java 驼峰（MyBatis-Plus 配置 map-underscore-to-camel-case 自动映射）。
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Data
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 邮箱（登录用，全局唯一） */
    private String email;

    /** 密码（bcrypt 哈希存储） */
    private String password;

    /** 昵称（2-20 字符，全局唯一） */
    private String nickname;

    /** 头像 URL */
    private String avatar;

    /** 个人简介（最多 200 字符） */
    private String bio;

    /** 角色: user / admin（预留 moderator） */
    private String role;

    /** 状态: 0=封禁, 1=正常 */
    private Integer status;

    /** 封禁原因（内部记录，不透露给用户） */
    private String banReason;

    /** 昵称上次修改时间，用于 30 天冷却控制 */
    private LocalDateTime nicknameUpdatedAt;

    /** 连续登录失败次数 */
    private Integer loginFailCount;

    /** 锁定截止时间（达 5 次失败时设置） */
    private LocalDateTime lockedUntil;

    /** 邮箱是否已验证 (0/1) */
    private Integer emailVerified;

    /** 注册时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 软删除标记，0=正常 1=已删除（@TableLogic 自动过滤） */
    @TableLogic
    private Integer deleted;
}
