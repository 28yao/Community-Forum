-- =============================================================
-- 开发环境：清理测试数据（保留 admin@forum.com + 3 个种子板块）
-- 禁止在生产环境执行
-- =============================================================

SET NAMES utf8mb4;

START TRANSACTION;

-- 互动与内容（全清）
DELETE FROM post_like;
DELETE FROM post_favorite;
DELETE FROM comment;
DELETE FROM post_image;
DELETE FROM post;

-- 三期公告
DELETE FROM announcement;

-- 二期扩展
DELETE FROM user_board_follow;
DELETE FROM board_application;

-- 注册验证 token
DELETE FROM verification_token;

-- 非种子板块（保留 id=1/2/3）
DELETE FROM board WHERE id NOT IN (1, 2, 3);

-- 非管理员用户（保留 id=1 admin@forum.com）
DELETE FROM user WHERE id <> 1;

-- 重置种子板块计数
UPDATE board SET post_count = 0, follower_count = 0 WHERE id IN (1, 2, 3);

COMMIT;
