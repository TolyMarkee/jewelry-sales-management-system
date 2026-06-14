package com.jewelry.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jewelry.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    // ====== 请在 application.yml 中配置你的大模型 API ======
    @Value("${ai.api.url:}")
    private String apiUrl;

    @Value("${ai.api.key:}")
    private String apiKey;

    @Value("${ai.api.model:gpt-3.5-turbo}")
    private String model;

    /**
     * AI 对话接口
     * 兼容 OpenAI / DeepSeek / 通义千问 等 API 格式
     */
    @PostMapping("/chat")
    public R chat(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String message = (String) params.get("message");
        if (message == null || message.isEmpty()) {
            return R.error("消息不能为空");
        }

        if (apiUrl.isEmpty() || apiKey.isEmpty()) {
            return R.ok("AI助手尚未配置：请在 application.yml 中设置 ai.api.url 和 ai.api.key")
                    .put("data", buildFallbackResponse(message));
        }

        try {
            // 构建系统提示词 - 珠宝销售助手
            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", "你是珠宝首饰销售管理系统的AI助手。回答格式要求：" +
                    "1. 每个句子用句号结束并换行。" +
                    "2. 重点标题用【标题】格式，不要用**加粗**。" +
                    "3. 数据用¥符号和数字呈现。" +
                    "4. 回答简洁，每条控制在200字以内。" +
                    "5. 保持专业和友善，用中文回答。");
            messages.add(systemMsg);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", message);
            messages.add(userMsg);

            // 构建请求体
            JSONObject reqBody = new JSONObject();
            reqBody.put("model", model);
            reqBody.put("messages", messages);
            reqBody.put("temperature", 0.7);
            reqBody.put("max_tokens", 1000);

            // 发送请求
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

            // 解析回复（兼容主流 API 格式）
            String reply = respJson.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            Map<String, Object> data = new HashMap<>();
            data.put("reply", reply);
            data.put("model", model);
            return R.ok().put("data", data);

        } catch (Exception e) {
            return R.error("AI服务调用失败: " + e.getMessage());
        }
    }

    /**
     * 未配置 API 时的模拟回复
     */
    private Map<String, Object> buildFallbackResponse(String message) {
        String reply;
        if (message.contains("天气") || message.contains("weather")) {
            reply = "您可以在仪表盘页面查看实时天气信息（需配置高德地图API Key）。";
        } else if (message.contains("商品") || message.contains("产品") || message.contains("珠宝")) {
            reply = "系统支持戒指、项链、手镯、耳环、手链等珠宝品类管理。您可以在「商品管理」页面查看和编辑所有商品信息。";
        } else if (message.contains("订单") || message.contains("销售")) {
            reply = "订单管理支持创建订单、状态流转（待付款→已付款→已发货→已完成）。创建订单时会自动扣减库存。";
        } else if (message.contains("客户")) {
            reply = "客户管理模块可以维护客户信息（姓名、电话、邮箱、地址），下单时需要选择客户。";
        } else if (message.contains("库存")) {
            reply = "库存管理支持入库和出库操作。当商品库存低于10件时，系统会发出预警提醒。";
        } else if (message.contains("你好") || message.contains("hi") || message.contains("hello")) {
            reply = "您好！我是珠宝销售管理系统的AI助手。有什么可以帮您的？您可以问我关于系统功能、珠宝知识、销售技巧等方面的问题。";
        } else {
            reply = "收到您的问题。如需更智能的回复，请配置大模型API（OpenAI/DeepSeek/通义千问等），在 application.yml 中设置 ai.api.url 和 ai.api.key。";
        }

        Map<String, Object> data = new HashMap<>();
        data.put("reply", reply);
        data.put("model", "local-fallback");
        return data;
    }
}
