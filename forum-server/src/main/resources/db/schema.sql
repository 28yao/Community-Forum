-- =============================================================
-- 社区论坛系统 - 建表脚本（schema.sql）
-- 关联 plan.md §3.2
-- 数据库字符集 utf8mb4_unicode_ci（在 database 级别已设置）
-- 一期 8 张表：user / board / post / post_image / comment
--             post_like / post_favorite / verification_token
-- 软删除策略：所有业务表均含 deleted TINYINT（plan.md §6.6）
-- =============================================================

-- ---------- 1. user 用户表（plan.md §3.2.1） ----------
CREATE TABLE IF NOT EXISTS `user` (
    `id`                   BIGINT       NOT NULL AUTO_INCREMENT             COMMENT '用户ID',
    `email`                VARCHAR(100) NOT NULL                            COMMENT '邮箱',
    `password`             VARCHAR(100) NOT NULL                            COMMENT '密码（bcrypt）',
    `nickname`             VARCHAR(20)  NOT NULL                            COMMENT '昵称',
    `avatar`               VARCHAR(255) DEFAULT NULL                        COMMENT '头像URL',
    `bio`                  VARCHAR(200) NOT NULL DEFAULT ''                 COMMENT '个人简介',
    `role`                 VARCHAR(20)  NOT NULL DEFAULT 'user'             COMMENT '角色: user / admin（预留 moderator）',
    `status`               TINYINT      NOT NULL DEFAULT 1                  COMMENT '状态: 0=封禁, 1=正常',
    `ban_reason`           VARCHAR(255) DEFAULT NULL                        COMMENT '封禁原因',
    `nickname_updated_at`  DATETIME     DEFAULT NULL                        COMMENT '昵称上次修改时间（30天冷却）',
    `login_fail_count`     INT          NOT NULL DEFAULT 0                  COMMENT '连续登录失败次数',
    `locked_until`         DATETIME     DEFAULT NULL                        COMMENT '锁定截止时间',
    `email_verified`       TINYINT      NOT NULL DEFAULT 0                  COMMENT '邮箱是否已验证',
    `created_at`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '注册时间',
    `updated_at`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                            ON UPDATE CURRENT_TIMESTAMP     COMMENT '更新时间',
    `deleted`              TINYINT      NOT NULL DEFAULT 0                  COMMENT '软删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_email`    (`email`),
    UNIQUE KEY `uk_nickname` (`nickname`),
    KEY `idx_status` (`status`),
    KEY `idx_role`   (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ---------- 2. board 版块表（plan.md §3.2.2） ----------
CREATE TABLE IF NOT EXISTS `board` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT             COMMENT '版块ID',
    `name`         VARCHAR(50)  NOT NULL                            COMMENT '版块名称',
    `description`  VARCHAR(200) NOT NULL DEFAULT ''                 COMMENT '版块描述',
    `sort_weight`  INT          NOT NULL DEFAULT 0                  COMMENT '排序权重（越大越前）',
    `status`       TINYINT      NOT NULL DEFAULT 1                  COMMENT '状态: 0=禁用, 1=启用',
    `post_count`   INT          NOT NULL DEFAULT 0                  COMMENT '帖子数量（冗余计数）',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP     COMMENT '更新时间',
    `deleted`      TINYINT      NOT NULL DEFAULT 0                  COMMENT '软删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_sort`       (`sort_weight` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='版块表';

-- ---------- 3. post 帖子表（plan.md §3.2.3） ----------
CREATE TABLE IF NOT EXISTS `post` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT             COMMENT '帖子ID',
    `board_id`         BIGINT       NOT NULL                            COMMENT '所属版块',
    `user_id`          BIGINT       NOT NULL                            COMMENT '发帖用户',
    `title`            VARCHAR(100) NOT NULL                            COMMENT '标题',
    `content`          TEXT         NOT NULL                            COMMENT '正文内容（富文本HTML）',
    `is_anonymous`     TINYINT      NOT NULL DEFAULT 0                  COMMENT '是否匿名（二期预留）',
    `is_pinned`        TINYINT      NOT NULL DEFAULT 0                  COMMENT '是否置顶',
    `is_featured`      TINYINT      NOT NULL DEFAULT 0                  COMMENT '是否加精',
    `like_count`       INT          NOT NULL DEFAULT 0                  COMMENT '点赞数（冗余）',
    `comment_count`    INT          NOT NULL DEFAULT 0                  COMMENT '评论数（冗余）',
    `view_count`       INT          NOT NULL DEFAULT 0                  COMMENT '浏览数',
    `is_edited`        TINYINT      NOT NULL DEFAULT 0                  COMMENT '是否编辑过',
    `last_edited_at`   DATETIME     DEFAULT NULL                        COMMENT '最后编辑时间',
    `status`           TINYINT      NOT NULL DEFAULT 1                  COMMENT '状态: 0=删除, 1=正常',
    `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '发帖时间',
    `updated_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                        ON UPDATE CURRENT_TIMESTAMP     COMMENT '更新时间',
    `deleted`          TINYINT      NOT NULL DEFAULT 0                  COMMENT '软删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_board_id`   (`board_id`, `status`, `is_pinned` DESC, `created_at` DESC),
    KEY `idx_user_id`    (`user_id`, `status`, `created_at` DESC),
    KEY `idx_created_at` (`created_at` DESC),
    FULLTEXT KEY `idx_search` (`title`, `content`)
        WITH PARSER ngram                                              -- 中文分词
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- ---------- 4. post_image 帖子图片表（plan.md §3.2.4） ----------
CREATE TABLE IF NOT EXISTS `post_image` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT             COMMENT '图片ID',
    `post_id`    BIGINT       NOT NULL                            COMMENT '所属帖子',
    `url`        VARCHAR(255) NOT NULL                            COMMENT '图片URL',
    `sort_order` INT          NOT NULL DEFAULT 0                  COMMENT '排序',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '上传时间',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子图片表';

-- ---------- 5. comment 评论表（plan.md §3.2.5） ----------
CREATE TABLE IF NOT EXISTS `comment` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT             COMMENT '评论ID',
    `post_id`           BIGINT        NOT NULL                            COMMENT '所属帖子',
    `user_id`           BIGINT        NOT NULL                            COMMENT '评论用户',
    `parent_id`         BIGINT        DEFAULT NULL                        COMMENT '父评论ID（NULL=顶级评论）',
    `reply_to_user_id`  BIGINT        DEFAULT NULL                        COMMENT '被回复的用户ID',
    `content`           VARCHAR(1000) NOT NULL                            COMMENT '评论内容',
    `depth`             TINYINT       NOT NULL DEFAULT 1                  COMMENT '层级: 1/2',
    `status`            TINYINT       NOT NULL DEFAULT 1                  COMMENT '状态: 0=删除, 1=正常',
    `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '评论时间',
    `updated_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                                                 ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT       NOT NULL DEFAULT 0                  COMMENT '软删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_post_id`   (`post_id`, `status`, `created_at` ASC),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_user_id`   (`user_id`, `status`, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ---------- 6. post_like 点赞表（plan.md §3.2.6） ----------
CREATE TABLE IF NOT EXISTS `post_like` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT             COMMENT 'ID',
    `post_id`    BIGINT   NOT NULL                            COMMENT '帖子ID',
    `user_id`    BIGINT   NOT NULL                            COMMENT '用户ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';

-- ---------- 7. post_favorite 收藏表（plan.md §3.2.7） ----------
CREATE TABLE IF NOT EXISTS `post_favorite` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT             COMMENT 'ID',
    `post_id`    BIGINT   NOT NULL                            COMMENT '帖子ID',
    `user_id`    BIGINT   NOT NULL                            COMMENT '用户ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- ---------- 8. verification_token 验证令牌表（plan.md §3.2.8） ----------
CREATE TABLE IF NOT EXISTS `verification_token` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT             COMMENT 'ID',
    `user_id`    BIGINT       DEFAULT NULL                        COMMENT '关联用户（注册验证时可能为空）',
    `email`      VARCHAR(100) NOT NULL                            COMMENT '邮箱',
    `token`      VARCHAR(64)  NOT NULL                            COMMENT '令牌',
    `type`       VARCHAR(20)  NOT NULL                            COMMENT '类型: register / reset_password',
    `used`       TINYINT      NOT NULL DEFAULT 0                  COMMENT '是否已使用',
    `expires_at` DATETIME     NOT NULL                            COMMENT '过期时间',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token`      (`token`),
    KEY `idx_email_type`       (`email`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证令牌表';
