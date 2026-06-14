package com.jewelry.config;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * XSS 跨站脚本攻击过滤器
 * 对所有请求参数进行 HTML 转义
 */
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(new XssRequestWrapper((HttpServletRequest) request), response);
    }

    private static class XssRequestWrapper extends HttpServletRequestWrapper {
        public XssRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) return null;
            String[] cleaned = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                cleaned[i] = clean(values[i]);
            }
            return cleaned;
        }

        @Override
        public String getParameter(String name) {
            return clean(super.getParameter(name));
        }

        @Override
        public String getHeader(String name) {
            return clean(super.getHeader(name));
        }

        private String clean(String value) {
            if (value == null) return null;
            return value
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;")
                    .replace("&", "&amp;")
                    .replace("(", "&#40;")
                    .replace(")", "&#41;");
        }
    }
}
