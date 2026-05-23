package com.forum.service.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 板块申请驳回请求 DTO（P2-M2，管理端）
 */
@Data
public class BoardApplicationRejectRequest {

    @NotBlank(message = "驳回原因不能为空")
    @Size(max = 200, message = "驳回原因最多 200 字")
    private String rejectReason;
}
