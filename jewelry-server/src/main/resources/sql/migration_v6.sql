-- 电商升级：成本价 + 利润追踪
USE jewelry_db;

ALTER TABLE `product` ADD COLUMN `cost_price` DECIMAL(10,2) DEFAULT 0 COMMENT '成本价' AFTER `price`;

-- 设置示例成本价（零售价 = 成本价 + 利润）
UPDATE `product` SET cost_price = 3999.00 WHERE id = 1;  -- 18K金钻石戒指 成本3999 售价5999
UPDATE `product` SET cost_price = 6500.00 WHERE id = 2;  -- Pt950铂金戒指 成本6500 售价8999
UPDATE `product` SET cost_price = 9000.00 WHERE id = 3;  -- 黄金项链24K 成本9000 售价12999
UPDATE `product` SET cost_price = 2200.00 WHERE id = 4;  -- 珍珠吊坠项链 成本2200 售价3599
UPDATE `product` SET cost_price = 14000.00 WHERE id = 5; -- 翡翠手镯 成本14000 售价19999
UPDATE `product` SET cost_price = 11000.00 WHERE id = 6; -- 黄金手镯 成本11000 售价15999
UPDATE `product` SET cost_price = 1800.00 WHERE id = 7;  -- 钻石耳钉 成本1800 售价2999
UPDATE `product` SET cost_price = 350.00 WHERE id = 8;   -- 银质手链 成本350 售价599
