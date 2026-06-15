package com.jewelry.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jewelry.common.R;
import com.jewelry.entity.*;
import com.jewelry.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/shop")
public class ShopController {

    @Autowired private ProductMapper productMapper;
    @Autowired private CategoryMapper categoryMapper;
    @Autowired private CustomerMapper customerMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderItemMapper orderItemMapper;

    /** 公开 - 商品列表 */
    @GetMapping("/products")
    public R products(@RequestParam(defaultValue = "") String keyword, @RequestParam(required = false) Long categoryId) {
        QueryWrapper<Product> w = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) w.like("name", keyword);
        if (categoryId != null) w.eq("category_id", categoryId);
        w.orderByDesc("create_time");
        List<Product> list = productMapper.selectList(w);
        // Fill category names
        for (Product p : list) {
            Category c = categoryMapper.selectById(p.getCategoryId());
            if (c != null) p.setCategoryName(c.getName());
        }
        return R.ok().data(list);
    }

    /** 公开 - 分类列表 */
    @GetMapping("/categories")
    public R categories() { return R.ok().data(categoryMapper.selectList(null)); }

    /** 公开 - 提交订单（客户自助下单） */
    @PostMapping("/order")
    public R placeOrder(@RequestBody Map<String, Object> params) {
        String name = (String) params.get("name");
        String phone = (String) params.get("phone");
        String email = (String) params.getOrDefault("email", "");
        String address = (String) params.getOrDefault("address", "");
        List<Map<String, Object>> items = (List<Map<String, Object>>) params.get("items");

        if (name == null || phone == null || items == null || items.isEmpty())
            return R.error("请填写完整信息");

        // 查找或创建客户
        Customer customer = customerMapper.selectOne(new QueryWrapper<Customer>().eq("phone", phone));
        if (customer == null) {
            customer = new Customer(); customer.setName(name); customer.setPhone(phone);
            customer.setEmail(email); customer.setAddress(address);
            customerMapper.insert(customer);
        }

        // 计算总价 & 创建订单
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> item : items) {
            Long pid = Long.parseLong(item.get("productId").toString());
            int qty = Integer.parseInt(item.get("quantity").toString());
            Product p = productMapper.selectById(pid);
            if (p != null) total = total.add(p.getPrice().multiply(BigDecimal.valueOf(qty)));
        }

        Order order = new Order();
        order.setOrderNo("SHOP" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        order.setCustomerId(customer.getId());
        order.setUserId(1L); // 系统自动下单
        order.setTotalAmount(total);
        order.setStatus("pending");
        order.setRemark("客户自助下单 - " + name + " " + phone);
        orderMapper.insert(order);

        for (Map<String, Object> item : items) {
            Long pid = Long.parseLong(item.get("productId").toString());
            int qty = Integer.parseInt(item.get("quantity").toString());
            Product p = productMapper.selectById(pid);
            OrderItem oi = new OrderItem(); oi.setOrderId(order.getId());
            oi.setProductId(pid); oi.setQuantity(qty);
            oi.setPrice(p != null ? p.getPrice() : BigDecimal.ZERO);
            orderItemMapper.insert(oi);
            // 扣库存
            if (p != null) { p.setStock(p.getStock() - qty); productMapper.updateById(p); }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", order.getOrderNo());
        data.put("totalAmount", total);
        return R.ok("下单成功").data(data);
    }
}
