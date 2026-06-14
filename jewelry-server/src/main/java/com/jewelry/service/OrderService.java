package com.jewelry.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jewelry.entity.Order;

import java.util.Map;

public interface OrderService extends IService<Order> {

    /**
     * 分页查询订单（含客户名、销售员名、订单明细）
     */
    Map<String, Object> selectWithRelations(Map<String, Object> params);

    /**
     * 创建订单（含明细）
     */
    Order createOrder(Order order);
}
