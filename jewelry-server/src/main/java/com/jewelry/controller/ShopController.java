package com.jewelry.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jewelry.common.JwtUtil;
import com.jewelry.common.R;
import com.jewelry.entity.*;
import com.jewelry.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    @GetMapping("/products")
    public R products(@RequestParam(defaultValue = "") String keyword, @RequestParam(required = false) Long categoryId) {
        QueryWrapper<Product> w = new QueryWrapper<>();
        if (!keyword.isEmpty()) w.like("name", keyword);
        if (categoryId != null) w.eq("category_id", categoryId);
        w.orderByDesc("create_time");
        List<Product> list = productMapper.selectList(w);
        for (Product p : list) { Category c = categoryMapper.selectById(p.getCategoryId()); if (c != null) p.setCategoryName(c.getName()); }
        return R.ok().data(list);
    }

    @GetMapping("/categories")
    public R categories() { return R.ok().data(categoryMapper.selectList(null)); }

    @PostMapping("/register")
    public R register(@RequestBody Map<String, String> params) {
        String name = params.get("name"), phone = params.get("phone"), password = params.get("password");
        if (name == null || phone == null || password == null || password.length() < 6) return R.error("请填写完整信息");
        if (customerMapper.selectCount(new QueryWrapper<Customer>().eq("phone", phone)) > 0) return R.error("该手机号已注册");
        Customer c = new Customer(); c.setName(name); c.setPhone(phone);
        c.setEmail(params.getOrDefault("email", "")); c.setAddress(params.getOrDefault("address", ""));
        c.setPassword(passwordEncoder.encode(password));
        customerMapper.insert(c);
        String token = jwtUtil.generateToken(c.getId(), c.getPhone());
        Map<String, Object> data = new HashMap<>(); data.put("token", token); data.put("customer", c);
        return R.ok("注册成功").data(data);
    }

    @PostMapping("/login")
    public R login(@RequestParam String phone, @RequestParam String password) {
        Customer c = customerMapper.selectOne(new QueryWrapper<Customer>().eq("phone", phone));
        if (c == null || !passwordEncoder.matches(password, c.getPassword())) return R.error("手机号或密码错误");
        String token = jwtUtil.generateToken(c.getId(), c.getPhone());
        Map<String, Object> data = new HashMap<>(); data.put("token", token); data.put("customer", c);
        return R.ok().data(data);
    }

    @GetMapping("/profile")
    public R profile(@RequestHeader("Authorization") String auth) {
        try { Long id = jwtUtil.getUserIdFromToken(auth.replace("Bearer ","")); return R.ok().data(customerMapper.selectById(id)); }
        catch (Exception e) { return R.error("请先登录"); }
    }

    @PostMapping("/order")
    public R placeOrder(@RequestBody Map<String, Object> params) {
        String name = (String) params.get("name"), phone = (String) params.get("phone");
        List<Map<String, Object>> items = (List<Map<String, Object>>) params.get("items");
        if (name == null || phone == null || items == null || items.isEmpty()) return R.error("请填写完整信息");

        Customer customer = customerMapper.selectOne(new QueryWrapper<Customer>().eq("phone", phone));
        if (customer == null) {
            customer = new Customer(); customer.setName(name); customer.setPhone(phone);
            customer.setEmail((String) params.getOrDefault("email", "")); customer.setAddress((String) params.getOrDefault("address", ""));
            customerMapper.insert(customer);
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> item : items) {
            Product p = productMapper.selectById(Long.parseLong(item.get("productId").toString()));
            if (p != null) total = total.add(p.getPrice().multiply(BigDecimal.valueOf(Integer.parseInt(item.get("quantity").toString()))));
        }

        Order order = new Order(); order.setOrderNo("SHOP" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        order.setCustomerId(customer.getId()); order.setUserId(1L); order.setTotalAmount(total);
        order.setStatus("pending"); order.setRemark("客户自助下单 - " + name + " " + phone);
        orderMapper.insert(order);

        for (Map<String, Object> item : items) {
            Long pid = Long.parseLong(item.get("productId").toString());
            int qty = Integer.parseInt(item.get("quantity").toString());
            Product p = productMapper.selectById(pid);
            OrderItem oi = new OrderItem(); oi.setOrderId(order.getId()); oi.setProductId(pid); oi.setQuantity(qty);
            oi.setPrice(p != null ? p.getPrice() : BigDecimal.ZERO); orderItemMapper.insert(oi);
            if (p != null) { p.setStock(p.getStock() - qty); productMapper.updateById(p); }
        }

        Map<String, Object> data = new HashMap<>(); data.put("orderNo", order.getOrderNo()); data.put("totalAmount", total); data.put("orderId", order.getId());
        return R.ok("下单成功，请完成支付").data(data);
    }

    /** 客户支付订单 */
    @PostMapping("/pay")
    public R payOrder(@RequestBody Map<String, Object> params) {
        Long orderId = Long.parseLong(params.get("orderId").toString());
        Order order = orderMapper.selectById(orderId);
        if (order == null) return R.error("订单不存在");
        if (!"pending".equals(order.getStatus())) return R.error("订单状态异常");
        order.setStatus("paid");
        orderMapper.updateById(order);
        return R.ok("支付成功");
    }

    /** 客户订单列表 */
    @GetMapping("/orders")
    public R myOrders(@RequestHeader("Authorization") String auth) {
        try {
            Long customerId = jwtUtil.getUserIdFromToken(auth.replace("Bearer ", ""));
            QueryWrapper<Order> w = new QueryWrapper<Order>().eq("customer_id", customerId).orderByDesc("create_time");
            return R.ok().data(orderMapper.selectList(w));
        } catch (Exception e) { return R.error("请先登录"); }
    }
}
