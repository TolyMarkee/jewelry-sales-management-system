package com.jewelry.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jewelry.common.R;
import com.jewelry.entity.User;
import com.jewelry.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class RegisterController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** 公开注册接口，无需登录 */
    @PostMapping("/register")
    public R register(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String realName = params.getOrDefault("realName", username);

        if (username == null || username.trim().isEmpty()) return R.error("用户名不能为空");
        if (password == null || password.length() < 6) return R.error("密码至少6位");

        // 检查用户名是否已存在
        Long exists = userMapper.selectCount(new QueryWrapper<User>().eq("username", username));
        if (exists > 0) return R.error("该用户名已被注册");

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName);
        user.setRole("sales");
        userMapper.insert(user);

        return R.ok("注册成功，请使用新账号登录");
    }
}
