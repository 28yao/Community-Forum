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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 软删除标记 */
    @TableLogic
    private Integer deleted;
}
