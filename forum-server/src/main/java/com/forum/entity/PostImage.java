package com.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子图片实体（plan.md §3.2.4）
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Data
@TableName("post_image")
public class PostImage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;
    private String url;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
