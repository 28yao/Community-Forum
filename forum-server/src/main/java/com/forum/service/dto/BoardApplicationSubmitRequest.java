package com.forum.service.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 板块申请提交请求 DTO（P2-M2）
 *
 * 字段长度对齐 board 表：name(5-20)、description(10-100)、icon、slogan(1-30)、tags（逗号分隔，60 内）
 */
@Data
public class BoardApplicationSubmitRequest {

    @NotBlank(message = "板块名称不能为空")
    @Size(min = 5, max = 20, message = "板块名称 5-20 字")
    private String name;

    @NotBlank(message = "板块描述不能为空")
    @Size(min = 10, max = 100, message = "板块描述 10-100 字")
    private String description;

    @Size(max = 255, message = "头像 URL 过长")
    private String icon;

    @Size(max = 30, message = "口号最多 30 字")
    private String slogan;

    /** 标签，逗号分隔最多 3 个，每个 1-10 字（应用层校验） */
    @Size(max = 60, message = "标签长度超限")
    private String tags;
}
