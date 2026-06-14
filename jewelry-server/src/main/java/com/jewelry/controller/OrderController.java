package com.jewelry.controller;

import com.jewelry.common.R;
import com.jewelry.entity.Order;
import com.jewelry.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 订单列表
     */
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        Map<String, Object> result = orderService.selectWithRelations(params);
        return R.ok().put("data", result);
    }

    /**
     * 创建订单
     */
    @PostMapping("/save")
    public R save(@RequestBody Order order, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        order.setUserId(userId);
        Order savedOrder = orderService.createOrder(order);
        return R.ok("下单成功").put("data", savedOrder);
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/updateStatus")
    public R updateStatus(@RequestBody Order order) {
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(order.getStatus());
        orderService.updateById(updateOrder);
        return R.ok("状态更新成功");
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable Long id) {
        orderService.removeById(id);
        return R.ok("删除成功");
    }
}
