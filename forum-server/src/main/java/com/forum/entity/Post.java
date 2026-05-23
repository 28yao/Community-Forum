package com.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子实体（plan.md §3.2.3）
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Data
@TableName("post")
public class Post implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long boardId;
    private Long userId;
    private String title;
    private String content;
    private Integer isAnonymous;
    private Integer isPinned;
    private Integer isFeatured;
    private Integer likeCount;
    private Integer commentCount;
    private Integer viewCount;
    private Integer isEdited;
    private LocalDateTime lastEditedAt;

    /** 0=删除/隐藏, 1=正常 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
