package com.jewelry.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简易限流拦截器
 * 防止暴力破解 / DDoS
 * 每个 IP 每分钟最多 60 次请求
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private final Map<String, RateWindow> ipMap = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String ip = getClientIp(request);
        long now = System.currentTimeMillis();

        RateWindow window = ipMap.computeIfAbsent(ip, k -> new RateWindow(now));

        synchronized (window) {
            if (now - window.start > 60_000) {
                window.start = now;
                window.count = 0;
            }
            window.count++;

            if (window.count > MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":429,\"msg\":\"请求过于频繁，请稍后再试\"}");
                return false;
            }
        }
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private static class RateWindow {
        long start;
        int count;
        RateWindow(long start) { this.start = start; this.count = 0; }
    }
}
