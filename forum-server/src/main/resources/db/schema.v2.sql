-- =============================================================
-- 社区论坛系统 — 二期数据库变更脚本（schema.v2.sql）
--
-- 二期范围：贴吧风 UI + 板块申请审核 + 板块关注 + 搜索增强 + 发帖弹窗
-- 设计依据：.context/phase2/prd.md §三 & plan.md §二
--
-- ⚠️ 执行前提：
--   1. 一期 schema.sql 和 seed.sql 已成功执行
--   2. user 表的系统管理员 id=1（一期 seed.sql 首条 INSERT 保证）
--   3. MySQL 8.x（用到了 ON UPDATE CURRENT_TIMESTAMP）
--
-- 幂等性：所有 ALTER / CREATE 都用 IF NOT EXISTS 或等价手段。重复执行不报错。
-- =============================================================

-- ============================================================================
-- P2-M1: 扩展一期 board 表
-- ============================================================================
-- 新增 5 个字段：icon / slogan / tags / owner_user_id / follower_count
-- 一期 3 个种子版块（id=1/2/3）的 owner_user_id 因 DEFAULT 1 自动落到系统管理员
--
-- ⚠️ MySQL 8 不支持 ADD COLUMN IF NOT EXISTS。本脚本仅首次执行；重复执行需手动跳过
-- 或先 DROP COLUMN。检查是否已执行过：SHOW COLUMNS FROM board LIKE 'icon';

ALTER TABLE `board`
    ADD COLUMN `icon`           VARCHAR(255) DEFAULT NULL                COMMENT '板块头像URL',
    ADD COLUMN `slogan`         VARCHAR(30)  DEFAULT NULL                COMMENT '板块口号',
    ADD COLUMN `tags`           VARCHAR(60)  DEFAULT NULL                COMMENT '标签，逗号分隔，最多3个',
    ADD COLUMN `owner_user_id`  BIGINT       NOT NULL DEFAULT 1          COMMENT '吧主用户ID（默认系统管理员）',
    ADD COLUMN `follower_count` INT          NOT NULL DEFAULT 0          COMMENT '关注数（冗余计数）';

ALTER TABLE `board` ADD INDEX `idx_owner` (`owner_user_id`);
ALTER TABLE `board` ADD INDEX `idx_followers` (`status`, `follower_count` DESC);


-- ============================================================================
-- P2-M2: 板块申请审核表
-- ============================================================================

CREATE TABLE IF NOT EXISTS `board_application` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT                  COMMENT '申请ID',
    `applicant_user_id` BIGINT       NOT NULL                                 COMMENT '申请人用户ID',
    `name`              VARCHAR(50)  NOT NULL                                 COMMENT '申请板块名（对齐 board.name 长度）',
    `description`       VARCHAR(200) NOT NULL                                 COMMENT '板块描述',
    `icon`              VARCHAR(255) DEFAULT NULL                             COMMENT '头像URL',
    `slogan`            VARCHAR(30)  DEFAULT NULL                             COMMENT '口号',
    `tags`              VARCHAR(60)  DEFAULT NULL                             COMMENT '标签',
    `status`            TINYINT      NOT NULL DEFAULT 1                       COMMENT '1=待审核 2=通过 3=驳回',
    `reject_reason`     VARCHAR(200) DEFAULT NULL                             COMMENT '驳回原因',
    `reviewer_admin_id` BIGINT       DEFAULT NULL                             COMMENT '审核管理员ID',
    `reviewed_at`       DATETIME     DEFAULT NULL                             COMMENT '审核时间',
    `board_id`          BIGINT       DEFAULT NULL                             COMMENT '审核通过后回写的板块ID',
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP       COMMENT '申请时间',
    PRIMARY KEY (`id`),
    KEY `idx_applicant_created` (`applicant_user_id`, `created_at` DESC),
    KEY `idx_status_created`    (`status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='板块申请记录';


-- ============================================================================
-- P2-M3: 用户-板块关注关系表
-- ============================================================================

CREATE TABLE IF NOT EXISTS `user_board_follow` (
    `user_id`    BIGINT   NOT NULL                                COMMENT '用户ID',
    `board_id`   BIGINT   NOT NULL                                COMMENT '板块ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '关注时间',
    PRIMARY KEY (`user_id`, `board_id`),
    KEY `idx_board_created` (`board_id`, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注板块';


-- ============================================================================
-- P2-M7: 标题专用 FULLTEXT 索引（仅 scope=title 时使用）
-- ============================================================================
-- ⚠️ 重要：必须带 WITH PARSER ngram 才能支持中文分词
-- 一期 CLAUDE.md 记录过此坑：SHOW INDEX 看不出 parser 是否落上，必须 SHOW CREATE TABLE 验证

ALTER TABLE `post` ADD FULLTEXT INDEX `idx_title` (`title`) WITH PARSER ngram;


-- =============================================================
-- 上线检查 SQL（手工执行验证）
-- =============================================================
-- 1. SHOW CREATE TABLE board\G       -- 检查 5 个新字段 + 2 个新索引
-- 2. SHOW CREATE TABLE post\G        -- 检查 idx_search 和 idx_title 都有 WITH PARSER ngram
-- 3. SELECT id, name, owner_user_id, follower_count FROM board;  -- 所有行 owner_user_id=1, follower_count=0
-- 4. SHOW CREATE TABLE board_application\G
-- 5. SHOW CREATE TABLE user_board_follow\G
