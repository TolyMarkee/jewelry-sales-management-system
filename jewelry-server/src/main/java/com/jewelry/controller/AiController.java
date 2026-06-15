package com.jewelry.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jewelry.common.R;
import com.jewelry.entity.Customer;
import com.jewelry.entity.Order;
import com.jewelry.entity.Product;
import com.jewelry.mapper.CustomerMapper;
import com.jewelry.mapper.OrderMapper;
import com.jewelry.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Value("${ai.api.url:}")
    private String apiUrl;
    @Value("${ai.api.key:}")
    private String apiKey;
    @Value("${ai.api.model:gpt-3.5-turbo}")
    private String model;

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private CustomerMapper customerMapper;

    @PostMapping("/chat")
    public R chat(@RequestBody Map<String, Object> params) {
        String message = (String) params.get("message");
        if (message == null || message.isEmpty()) return R.error("消息不能为空");

        if (apiUrl.isEmpty() || apiKey.isEmpty()) {
            return R.ok("AI助手尚未配置").data(buildFallbackResponse(message));
        }

        try {
            // Build system context with real database data
            String context = buildSystemContext();

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", "你是珠宝首饰销售管理系统的AI助手。以下是系统实时数据，请基于这些数据回答用户问题。" +
                    "不要编造数字，使用下方提供的真实数据。" +
                    "回答格式：每个句号后换行，重点标题用【标题】格式。" +
                    "用中文回答，简洁专业。\n\n" + context);
            messages.add(systemMsg);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", message);
            messages.add(userMsg);

            JSONObject reqBody = new JSONObject();
            reqBody.put("model", model);
            reqBody.put("messages", messages);
            reqBody.put("temperature", 0.5);
            reqBody.put("max_tokens", 800);

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(reqBody.toJSONString().getBytes(StandardCharsets.UTF_8));
            }

            String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            JSONObject respJson = JSON.parseObject(response);
            String reply = respJson.getJSONArray("choices").getJSONObject(0)
                    .getJSONObject("message").getString("content");

            Map<String, Object> data = new HashMap<>();
            data.put("reply", reply);
            data.put("model", model);
            return R.ok().data(data);

        } catch (Exception e) {
            return R.error("AI服务调用失败: " + e.getMessage());
        }
    }

    /**
     * Build real-time system context from database
     */
    private String buildSystemContext() {
        StringBuilder ctx = new StringBuilder();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        // Products
        long productTotal = productMapper.selectCount(null);
        List<Product> lowStock = productMapper.selectList(
                new QueryWrapper<Product>().lt("stock", 10).orderByAsc("stock"));
        List<Product> topProducts = productMapper.selectList(
                new QueryWrapper<Product>().orderByDesc("price").last("LIMIT 5"));

        ctx.append("【实时系统数据】\n");
        ctx.append("日期：").append(today).append("\n");
        ctx.append("商品总数：").append(productTotal).append("件\n");
        if (!lowStock.isEmpty()) {
            ctx.append("库存预警（<10件）：");
            for (Product p : lowStock) ctx.append(p.getName()).append("(库存").append(p.getStock()).append(") ");
            ctx.append("\n");
        }
        ctx.append("高价商品TOP5：");
        for (Product p : topProducts) ctx.append(p.getName()).append(" ¥").append(p.getPrice()).append(" ");
        ctx.append("\n");

        // Orders
        long orderTotal = orderMapper.selectCount(null);
        QueryWrapper<Order> todayWrapper = new QueryWrapper<>();
        todayWrapper.ge("create_time", today + " 00:00:00");
        long todayOrders = orderMapper.selectCount(todayWrapper);
        double todaySales = 0;
        List<Order> todayList = orderMapper.selectList(todayWrapper);
        for (Order o : todayList) {
            if (!"cancelled".equals(o.getStatus())) {
                todaySales += o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0;
            }
        }

        ctx.append("订单总数：").append(orderTotal).append("笔\n");
        ctx.append("今日订单：").append(todayOrders).append("笔，销售额 ¥").append(String.format("%.2f", todaySales)).append("\n");

        // Customers
        long customerTotal = customerMapper.selectCount(null);
        ctx.append("客户总数：").append(customerTotal).append("人\n");

        return ctx.toString();
    }

    private Map<String, Object> buildFallbackResponse(String message) {
        String reply;
        if (message.contains("商品")) reply = "系统支持戒指、项链、手镯、耳环、手链等珠宝品类管理。";
        else if (message.contains("订单") || message.contains("销售")) reply = "订单管理支持创建订单、状态流转（待付款→已付款→已发货→已完成）。";
        else if (message.contains("库存")) reply = "库存管理支持入库和出库操作。库存低于10件时系统会预警。";
        else if (message.contains("你好") || message.contains("hi")) reply = "您好！我是珠宝销售管理系统的AI助手，有什么可以帮您？";
        else reply = "收到您的问题。如需实时数据查询，请确保已配置DeepSeek API。";

        Map<String, Object> data = new HashMap<>();
        data.put("reply", reply);
        data.put("model", "local-fallback");
        return data;
    }
}
