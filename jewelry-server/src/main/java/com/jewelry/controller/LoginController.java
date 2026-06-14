package com.jewelry.controller;

import com.jewelry.common.R;
import com.jewelry.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * 登录接口
     * POST /api/login
     */
    @PostMapping("/api/login")
    public R login(@RequestParam Map<String, Object> params) {
        String username = (String) params.get("username");
        String password = (String) params.get("password");

        if (username == null || password == null) {
            return R.error("用户名和密码不能为空");
        }

        Map<String, Object> result = userService.login(username, password);
        if (result == null) {
            return R.error("用户名或密码错误");
        }

        return R.ok().put("data", result);
    }
}
