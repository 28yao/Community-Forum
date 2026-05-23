package com.forum.service.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 发帖请求 DTO
 */
@Data
public class CreatePostRequest {

    @NotNull(message = "请选择版块")
    private Long boardId;

    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 100, message = "标题长度 2-100 字符")
    private String title;

    @NotBlank(message = "正文不能为空")
    private String content;

    /** 图片 URL 列表（最多 9 张） */
    private List<String> imageUrls;
}
