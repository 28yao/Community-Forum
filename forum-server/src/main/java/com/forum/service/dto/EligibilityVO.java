package com.forum.service.dto;

import lombok.Data;

/**
 * 板块申请资格 VO（P2-M2）
 *
 * 供前端在打开申请表单前判断用户是否够格，并把差距告诉用户。
 */
@Data
public class EligibilityVO {

    /** 是否达标 */
    private boolean eligible;

    /** 注册天数 */
    private long registeredDays;

    /** 已发帖数（status=1） */
    private long postCount;

    /** 是否已有待审核申请 */
    private boolean hasPending;

    /** 不达标时的友好原因（前端直接展示） */
    private String reason;

    /** 门槛：注册天数 ≥ 此值 */
    private int requiredDays;

    /** 门槛：发帖数 ≥ 此值 */
    private int requiredPosts;
}
