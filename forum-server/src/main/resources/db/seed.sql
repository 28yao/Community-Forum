-- =============================================================
-- 社区论坛系统 - 种子数据（seed.sql）
-- C8 决策：插入 1 个默认管理员 + 3 个版块
--
-- ⚠️ 安全提示：
--   默认管理员密码 = 123456（bcrypt 已加密；明文仅用于开发期首次登录）
--   该密码不符合 PRD §5.1.1 强度规则（需字母+数字），仅供开发；
--   生产环境部署前必须改为强密码，或首次登录后强制修改。
-- =============================================================

-- ---------- 默认管理员（C8） ----------
-- 邮箱: admin@forum.com
-- 密码: 123456（bcrypt: $2a$10$pwgP/ltBYyMKTGmXhSIhAOx9G4NtpFUw3rfZ.2tX4i1go7TTBqUTO）
-- 邮箱已标记为已验证，无需走注册验证流程
INSERT INTO `user` (`email`, `password`, `nickname`, `role`, `status`, `email_verified`, `bio`)
VALUES ('admin@forum.com',
        '$2a$10$pwgP/ltBYyMKTGmXhSIhAOx9G4NtpFUw3rfZ.2tX4i1go7TTBqUTO',
        '系统管理员',
        'admin',
        1,
        1,
        '社区论坛系统管理员账号')
ON DUPLICATE KEY UPDATE `email` = `email`;   -- 重复执行幂等：邮箱已存在则不动

-- ---------- 3 个种子版块 ----------
INSERT INTO `board` (`name`, `description`, `sort_weight`, `status`)
VALUES ('技术交流', '编程、架构、工具与最佳实践讨论',     100, 1),
       ('生活日常', '日常分享、生活感悟、闲聊吐槽',       80,  1),
       ('资源分享', '电子书、教程、工具、网站等资源分享', 60,  1)
ON DUPLICATE KEY UPDATE `name` = `name`;     -- 重复执行幂等
