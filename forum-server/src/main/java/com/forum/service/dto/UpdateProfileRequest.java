package com.forum.service.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 更新个人资料请求 DTO
 */
@Data
public class UpdateProfileRequest {

    @Size(min = 2, max = 20, message = "昵称长度 2-20 字符")
    private String nickname;

    @Size(max = 200, message = "简介最多 200 字符")
    private String bio;
}
