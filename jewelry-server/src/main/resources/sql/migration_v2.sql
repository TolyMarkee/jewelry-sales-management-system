-- =============================================
-- 用户表扩展 & 文件上传支持
-- 在已有 jewelry_db 上执行此脚本
-- =============================================
USE jewelry_db;

-- 给 user 表添加邮箱和头像字段
ALTER TABLE `user` ADD COLUMN `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱' AFTER `phone`;
ALTER TABLE `user` ADD COLUMN `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像路径' AFTER `email`;

-- 给 admin 更新示例数据
UPDATE `user` SET email = 'admin@jewelry.com' WHERE id = 1;
UPDATE `user` SET email = 'zhangsan@jewelry.com' WHERE id = 2;
