-- =============================================================
-- Phase 3: 公告表（站点级 / 板块级双层）
-- =============================================================

CREATE TABLE IF NOT EXISTS announcement (
    id              BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    scope           VARCHAR(10) NOT NULL COMMENT '作用域：site/board',
    board_id        BIGINT UNSIGNED DEFAULT NULL COMMENT 'scope=board 时必填，scope=site 时为 NULL',
    title           VARCHAR(100) NOT NULL COMMENT '公告标题',
    content         TEXT NOT NULL COMMENT '公告正文（HTML）',
    pinned          TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：0=否 1=是',
    sort_weight     INT NOT NULL DEFAULT 0 COMMENT '排序权重（越大越靠前）',
    publisher_id    BIGINT UNSIGNED NOT NULL COMMENT '发布人 user_id',
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=显示 0=隐藏',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT NOT NULL DEFAULT 0 COMMENT '软删除：0=正常 1=已删',
    INDEX idx_scope_board (scope, board_id, status, deleted),
    INDEX idx_publisher (publisher_id),
    INDEX idx_created (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='公告（站点级 / 板块级双层）';
