package com.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 版块实体
 *
 * 对应 board 表（plan.md §3.2.2）。
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Data
@TableName("board")
public class Board implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 版块ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 版块名称（唯一） */
    private String name;

    /** 版块描述 */
    private String description;

    /** 排序权重（越大越前） */
    private Integer sortWeight;

    /** 状态: 0=禁用, 1=启用 */
    private Integer status;

    /** 帖子数量（冗余计数） */
    private Integer postCount;

    /** 板块头像URL（P2-M1） */
    private String icon;

    /** 板块口号（P2-M1） */
    private String slogan;

    /** 标签，逗号分隔，最多 3 个（P2-M1） */
    private String tags;

    /** 吧主用户ID，默认系统管理员（P2-M1） */
    private Long ownerUserId;

    /** 关注数（冗余计数，P2-M1） */
    private Integer followerCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 软删除标记 */
    @TableLogic
    private Integer deleted;
}
