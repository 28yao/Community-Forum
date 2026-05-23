package com.forum.service.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 创建评论请求
 */
@Data
public class CreateCommentRequest {

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容不能超过 1000 字")
    private String content;

    /** 可选：父评论 ID（顶级评论留空） */
    private Long parentId;

    /** 可选：被回复用户 ID（depth=2 时使用） */
    private Long replyToUserId;
}
