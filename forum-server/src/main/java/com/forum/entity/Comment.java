package com.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论实体（对应 comment 表，邻接表 + depth 限制 2 层）
 */
@Data
@TableName("comment")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;
    private Long userId;
    /** 父评论 ID，NULL 表示顶级 */
    private Long parentId;
    /** 被回复的用户 ID（depth=2 时使用） */
    private Long replyToUserId;
    private String content;
    /** 层级：1=顶级，2=子级 */
    private Integer depth;
    /** 状态：0=删除，1=正常 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
