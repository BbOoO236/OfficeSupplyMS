-- 为用户表添加登录锁定字段
-- error_count: 密码连续错误次数
-- lock_until: 账户锁定截止时间

ALTER TABLE `user`
ADD COLUMN `error_count` INT NOT NULL DEFAULT 0 COMMENT '密码连续错误次数' AFTER `status`,
ADD COLUMN `lock_until` DATETIME DEFAULT NULL COMMENT '账户锁定截止时间' AFTER `error_count`;

-- 为admin和emp001用户也添加锁定字段（默认值为0和null）
UPDATE `user` SET `error_count` = 0, `lock_until` = NULL WHERE `error_count` IS NULL OR `lock_until` IS NULL;
