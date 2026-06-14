package com.jewelry.common;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一响应结果封装
 */
public class R extends HashMap<String, Object> {

    public R() {
        put("code", 0);
        put("msg", "success");
    }

    public static R ok() {
        return new R();
    }

    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    public static R error() {
        return error(500, "服务器异常，请稍后重试");
    }

    public static R error(String msg) {
        return error(500, msg);
    }

    public static R error(int code, String msg) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public R data(Object data) {
        return put("data", data);
    }

    public int getCode() {
        return (int) get("code");
    }
}
