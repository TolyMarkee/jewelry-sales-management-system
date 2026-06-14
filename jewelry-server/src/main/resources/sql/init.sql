-- =============================================
-- 珠宝首饰销售管理系统 - 数据库初始化脚本
-- =============================================

CREATE DATABASE IF NOT EXISTS jewelry_db DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE jewelry_db;

-- ----------------------------
-- 1. 用户表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `role` VARCHAR(20) NOT NULL DEFAULT 'sales' COMMENT '角色: admin/sales',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 默认管理员账号: admin / 123456
INSERT INTO `user` VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', '13800000000', 'admin', NOW());
INSERT INTO `user` VALUES (2, 'sales01', 'e10adc3949ba59abbe56e057f20f883e', '销售员张三', '13800000001', 'sales', NOW());

-- ----------------------------
-- 2. 商品分类表
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '分类描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

INSERT INTO `category` VALUES (1, '戒指', '各类钻石、黄金戒指', NOW());
INSERT INTO `category` VALUES (2, '项链', '精美项链、吊坠', NOW());
INSERT INTO `category` VALUES (3, '手镯', '黄金、玉石手镯', NOW());
INSERT INTO `category` VALUES (4, '耳环', '时尚耳环、耳钉', NOW());
INSERT INTO `category` VALUES (5, '手链', '各类手链饰品', NOW());

-- ----------------------------
-- 3. 商品表
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `material` VARCHAR(50) DEFAULT NULL COMMENT '材质',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `stock` INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    `image` VARCHAR(255) DEFAULT NULL COMMENT '图片URL',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '商品描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

INSERT INTO `product` VALUES (1, '18K金钻石戒指', 1, '18K金/钻石', 5999.00, 20, 'ring-01.svg', '精美18K金镶嵌钻石戒指，适合求婚、订婚', NOW());
INSERT INTO `product` VALUES (2, 'Pt950铂金戒指', 1, '铂金', 8999.00, 15, 'ring-02.svg', 'Pt950铂金戒指，简约大方', NOW());
INSERT INTO `product` VALUES (3, '黄金项链24K', 2, '24K黄金', 12999.00, 10, 'necklace-01.svg', '24K纯金项链，经典款式', NOW());
INSERT INTO `product` VALUES (4, '珍珠吊坠项链', 2, '18K金/珍珠', 3599.00, 25, 'necklace-02.svg', '天然珍珠吊坠，18K金链', NOW());
INSERT INTO `product` VALUES (5, '翡翠手镯', 3, '天然翡翠', 19999.00, 8, 'bracelet-01.svg', '天然A货翡翠手镯，冰种飘花', NOW());
INSERT INTO `product` VALUES (6, '黄金手镯', 3, '24K黄金', 15999.00, 12, 'bracelet-01.svg', '足金手镯，传统工艺', NOW());
INSERT INTO `product` VALUES (7, '钻石耳钉', 4, '18K金/钻石', 2999.00, 30, 'earring-01.svg', '闪耀钻石耳钉，日常百搭', NOW());
INSERT INTO `product` VALUES (8, '银质手链', 5, '925银', 599.00, 50, 'chain-01.svg', '925银手链，时尚简约', NOW());

-- ----------------------------
-- 4. 客户表
-- ----------------------------
DROP TABLE IF EXISTS `customer`;
CREATE TABLE `customer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '客户ID',
    `name` VARCHAR(50) NOT NULL COMMENT '客户姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '电子邮箱',
    `address` VARCHAR(200) DEFAULT NULL COMMENT '地址',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

INSERT INTO `customer` VALUES (1, '李先生', '13900000001', 'li@example.com', '北京市朝阳区', NOW());
INSERT INTO `customer` VALUES (2, '王女士', '13900000002', 'wang@example.com', '上海市浦东新区', NOW());
INSERT INTO `customer` VALUES (3, '赵先生', '13900000003', 'zhao@example.com', '广州市天河区', NOW());

-- ----------------------------
-- 5. 订单表
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
    `customer_id` BIGINT NOT NULL COMMENT '客户ID',
    `user_id` BIGINT NOT NULL COMMENT '销售员ID',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/paid/shipped/completed/cancelled',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ----------------------------
-- 6. 订单明细表
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
    `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ----------------------------
-- 7. 库存记录表
-- ----------------------------
DROP TABLE IF EXISTS `stock_record`;
CREATE TABLE `stock_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `type` VARCHAR(20) NOT NULL COMMENT '类型: in/out',
    `quantity` INT NOT NULL COMMENT '数量',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存记录表';
