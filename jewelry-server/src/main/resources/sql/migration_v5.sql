-- 客户账号体系
USE jewelry_db;

ALTER TABLE `customer` ADD COLUMN `password` VARCHAR(100) DEFAULT NULL COMMENT '登录密码(BCrypt)' AFTER `email`;

-- 给已有客户设置默认密码 123456
UPDATE `customer` SET password = '$2a$10$4Bz4b.Tg6IHv9buqgw2ELeHP3ldaWLBISwBnbF8o6dM0meErO1ixq' WHERE id IN (1,2,3);
