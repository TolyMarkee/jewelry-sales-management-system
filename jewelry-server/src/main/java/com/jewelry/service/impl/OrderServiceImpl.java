package com.jewelry.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jewelry.entity.Order;
import com.jewelry.entity.OrderItem;
import com.jewelry.entity.Product;
import com.jewelry.mapper.OrderItemMapper;
import com.jewelry.mapper.OrderMapper;
import com.jewelry.mapper.ProductMapper;
import com.jewelry.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public Map<String, Object> selectWithRelations(Map<String, Object> params) {
        int pageNum = Integer.parseInt(params.getOrDefault("page", "1").toString());
        int limit = Integer.parseInt(params.getOrDefault("limit", "10").toString());
        String orderNo = params.getOrDefault("orderNo", "").toString();
        String status = params.getOrDefault("status", "").toString();

        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like("o.order_no", orderNo);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("o.status", status);
        }
        wrapper.orderByDesc("o.create_time");

        Page<Order> page = new Page<>(pageNum, limit);
        IPage<Order> result = baseMapper.selectWithRelations(page, wrapper);

        // 查询每个订单的明细
        for (Order order : result.getRecords()) {
            QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
            itemWrapper.eq("order_id", order.getId());
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
            for (OrderItem item : items) {
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    item.setProductName(product.getName());
                }
            }
            order.setItems(items);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("total", result.getTotal());
        map.put("list", result.getRecords());
        return map;
    }

    @Override
    @Transactional
    public Order createOrder(Order order) {
        String orderNo = "JWL" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        order.setOrderNo(orderNo);
        order.setStatus("pending");
        baseMapper.insert(order);

        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setOrderId(order.getId());
                orderItemMapper.insert(item);

                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    product.setStock(product.getStock() - item.getQuantity());
                    productMapper.updateById(product);
                }
            }
        }
        return order;
    }
}
