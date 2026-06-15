-- =============================================
-- AI助手每日限额 + 角色区分
-- =============================================
USE jewelry_db;

-- AI使用记录表
DROP TABLE IF EXISTS `ai_usage`;
CREATE TABLE `ai_usage` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `usage_date` DATE NOT NULL COMMENT '使用日期',
    `count` INT NOT NULL DEFAULT 0 COMMENT '当日提问次数',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `usage_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI使用记录表';

-- 测试账号（密码均为 123456 的 BCrypt 哈希）
-- 销售员 sales01 已存在，新增一个次级管理员和一个普通销售员

-- 次级管理员（销售主管）
INSERT INTO `user` VALUES (4, 'mgr01', '$2a$10$4Bz4b.Tg6IHv9buqgw2ELeHP3ldaWLBISwBnbF8o6dM0meErO1ixq', '王主管', '13800000002', 'mgr@jewelry.com', NULL, 'manager', NOW());

-- 普通销售员
INSERT INTO `user` VALUES (5, 'sales03', '$2a$10$4Bz4b.Tg6IHv9buqgw2ELeHP3ldaWLBISwBnbF8o6dM0meErO1ixq', '赵销售', '13800000003', 'zhao@jewelry.com', NULL, 'sales', NOW());
