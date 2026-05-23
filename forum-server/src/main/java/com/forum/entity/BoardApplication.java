package com.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 板块申请记录实体（P2-M2）
 *
 * 对应 board_application 表。
 * 状态机：1=待审核 → 2=通过 / 3=驳回。驳回后用户可改后重提，生成新记录。
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Data
@TableName("board_application")
public class BoardApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申请人用户ID */
    private Long applicantUserId;

    /** 申请的板块名 */
    private String name;

    /** 板块描述 */
    private String description;

    private String icon;
    private String slogan;
    private String tags;

    /** 状态：1=待审核 2=通过 3=驳回 */
    private Integer status;

    /** 驳回原因（仅 status=3） */
    private String rejectReason;

    /** 审核人 admin id（status≠1 时有值） */
    private Long reviewerAdminId;

    private LocalDateTime reviewedAt;

    /** 审核通过后回写的板块 id（追溯用） */
    private Long boardId;

    private LocalDateTime createdAt;
}
