package com.jewelry.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jewelry.common.R;
import com.jewelry.entity.Order;
import com.jewelry.entity.Product;
import com.jewelry.entity.User;
import com.jewelry.mapper.CustomerMapper;
import com.jewelry.mapper.OrderMapper;
import com.jewelry.mapper.ProductMapper;
import com.jewelry.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Value("${ai.api.url:}") private String apiUrl;
    @Value("${ai.api.key:}") private String apiKey;
    @Value("${ai.api.model:gpt-3.5-turbo}") private String model;

    @Autowired private ProductMapper productMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private CustomerMapper customerMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private JdbcTemplate jdbc;

    private static final int DAILY_LIMIT = 30;

    @PostMapping("/chat")
    public R chat(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String message = (String) params.get("message");
        if (message == null || message.isEmpty()) return R.error("消息不能为空");

        Long userId = (Long) request.getAttribute("userId");
        User user = userMapper.selectById(userId);
        if (user == null) return R.error("用户不存在");

        // Check daily limit
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        int used = getTodayUsage(userId, today);
        if (used >= DAILY_LIMIT) {
            return R.ok("今日提问次数已用完（" + DAILY_LIMIT + "/" + DAILY_LIMIT + "），请明天再来。")
                    .data(buildLimitResponse());
        }

        if (apiUrl.isEmpty() || apiKey.isEmpty()) {
            return R.ok("AI助手尚未配置").data(buildFallbackResponse(message, user, used));
        }

        try {
            // Build role-specific context
            String context = user.getRole().equals("admin") ? buildAdminContext()
                    : buildSalesContext(user.getId(), user.getRealName());

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> sys = new HashMap<>();
            sys.put("role", "system");
            sys.put("content", (user.getRole().equals("admin")
                    ? "你是珠宝销售管理系统的【管理员AI助手】。可查看全部数据。"
                    : "你是珠宝销售管理系统的【销售员AI助手】。仅提供销售相关建议。")
                    + "回答格式：每个句号后换行，重点用【标题】格式。用中文，简洁专业。\n\n" + context);
            messages.add(sys);

            Map<String, String> um = new HashMap<>();
            um.put("role", "user");
            um.put("content", message);
            messages.add(um);

            JSONObject reqBody = new JSONObject();
            reqBody.put("model", model);
            reqBody.put("messages", messages);
            reqBody.put("temperature", 0.5);
            reqBody.put("max_tokens", 600);

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

            String resp = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String reply = JSON.parseObject(resp).getJSONArray("choices").getJSONObject(0)
                    .getJSONObject("message").getString("content");

            // Save to history
            saveMessage(userId, "user", message);
            saveMessage(userId, "assistant", reply);
            incrementUsage(userId, today);

            Map<String, Object> data = new HashMap<>();
            data.put("reply", reply);
            data.put("model", model);
            data.put("remaining", DAILY_LIMIT - used - 1);
            return R.ok().data(data);

        } catch (Exception e) {
            return R.error("AI服务调用失败: " + e.getMessage());
        }
    }

    /** Get remaining questions for current user */
    @GetMapping("/remaining")
    public R remaining(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        int used = getTodayUsage(userId, today);
        Map<String, Object> data = new HashMap<>();
        data.put("used", used);
        data.put("remaining", DAILY_LIMIT - used);
        data.put("limit", DAILY_LIMIT);
        return R.ok().data(data);
    }

    /** Load user's chat history */
    @GetMapping("/history")
    public R history(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Map<String, Object>> messages = jdbc.query(
                "SELECT role, content FROM ai_message WHERE user_id=? ORDER BY create_time ASC",
                (rs, rowNum) -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("role", rs.getString("role"));
                    m.put("content", rs.getString("content"));
                    return m;
                }, userId);
        return R.ok().data(messages);
    }

    /** Delete all chat history for current user */
    @DeleteMapping("/history")
    public R deleteHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        jdbc.update("DELETE FROM ai_message WHERE user_id=?", userId);
        return R.ok("历史记录已清除");
    }

    private void saveMessage(Long userId, String role, String content) {
        jdbc.update("INSERT INTO ai_message (user_id, role, content) VALUES (?, ?, ?)",
                userId, role, content);
    }

    private int getTodayUsage(Long userId, String date) {
        List<Integer> results = jdbc.query(
                "SELECT count FROM ai_usage WHERE user_id=? AND usage_date=?",
                (rs, rowNum) -> rs.getInt("count"), userId, date);
        return results.isEmpty() ? 0 : results.get(0);
    }

    private void incrementUsage(Long userId, String date) {
        jdbc.update("INSERT INTO ai_usage (user_id, usage_date, count) VALUES (?, ?, 1) " +
                "ON DUPLICATE KEY UPDATE count = count + 1", userId, date);
    }

    private String buildAdminContext() {
        StringBuilder ctx = new StringBuilder();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        long prodCount = productMapper.selectCount(null);
        List<Product> lowStock = productMapper.selectList(
                new QueryWrapper<Product>().lt("stock", 10).orderByAsc("stock"));
        long orderCount = orderMapper.selectCount(null);
        QueryWrapper<Order> tw = new QueryWrapper<Order>().ge("create_time", today + " 00:00:00");
        long todayOrders = orderMapper.selectCount(tw);
        double todaySales = 0;
        for (Order o : orderMapper.selectList(tw))
            if (!"cancelled".equals(o.getStatus()))
                todaySales += o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0;
        long custCount = customerMapper.selectCount(null);

        ctx.append("【管理员实时数据】\n日期：").append(today).append("\n商品总数：").append(prodCount).append("件\n");
        if (!lowStock.isEmpty()) {
            ctx.append("库存预警：");
            for (Product p : lowStock) ctx.append(p.getName()).append("(").append(p.getStock()).append(") ");
            ctx.append("\n");
        }
        ctx.append("订单总数：").append(orderCount).append("笔，今日").append(todayOrders).append("笔 ¥").append(String.format("%.2f", todaySales)).append("\n");
        ctx.append("客户总数：").append(custCount).append("人\n");
        return ctx.toString();
    }

    private String buildSalesContext(Long userId, String name) {
        StringBuilder ctx = new StringBuilder();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        QueryWrapper<Order> uw = new QueryWrapper<Order>().eq("user_id", userId);
        long myTotal = orderMapper.selectCount(uw);
        QueryWrapper<Order> tw = new QueryWrapper<Order>().eq("user_id", userId).ge("create_time", today + " 00:00:00");
        long myToday = orderMapper.selectCount(tw);
        double mySales = 0;
        for (Order o : orderMapper.selectList(tw))
            if (!"cancelled".equals(o.getStatus()))
                mySales += o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0;

        ctx.append("【").append(name).append("的销售数据】\n日期：").append(today).append("\n");
        ctx.append("我的订单：累计").append(myTotal).append("笔，今日").append(myToday).append("笔 ¥").append(String.format("%.2f", mySales)).append("\n");
        List<Product> lowStock = productMapper.selectList(new QueryWrapper<Product>().lt("stock", 10));
        if (!lowStock.isEmpty()) {
            ctx.append("库存预警：");
            for (Product p : lowStock) ctx.append(p.getName()).append(" ");
            ctx.append("\n");
        }
        return ctx.toString();
    }

    private Map<String, Object> buildLimitResponse() {
        Map<String, Object> d = new HashMap<>();
        d.put("reply", "今日提问次数已用完（" + DAILY_LIMIT + "次），明天自动重置。");
        d.put("remaining", 0);
        return d;
    }

    private Map<String, Object> buildFallbackResponse(String msg, User user, int used) {
        Map<String, Object> d = new HashMap<>();
        String role = user.getRole().equals("admin") ? "管理员" : "销售员";
        d.put("reply", "您好，" + role + user.getRealName() + "！AI助手未配置大模型API。当前使用离线模式，无法查询实时数据。");
        d.put("remaining", DAILY_LIMIT - used);
        return d;
    }
}
