package com.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("announcement")
public class Announcement implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作用域：site / board */
    private String scope;

    /** scope=board 时关联的板块 ID */
    private Long boardId;

    private String title;

    /** 公告正文（HTML） */
    private String content;

    /** 是否置顶 */
    private Integer pinned;

    /** 排序权重 */
    private Integer sortWeight;

    /** 发布人 user_id */
    private Long publisherId;

    /** 1=显示 0=隐藏 */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
