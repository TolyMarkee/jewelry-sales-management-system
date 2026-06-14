package com.jewelry.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jewelry.common.JwtUtil;
import com.jewelry.entity.User;
import com.jewelry.mapper.UserMapper;
import com.jewelry.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Map<String, Object> login(String username, String password) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        User user = baseMapper.selectOne(wrapper);

        if (user == null) {
            return null;
        }

        String stored = user.getPassword();
        boolean matched = false;

        // BCrypt 格式：$2a$10$...
        if (stored != null && stored.startsWith("$2a$")) {
            matched = passwordEncoder.matches(password, stored);
        } else {
            // 兼容旧的 MD5 密码，验证成功后自动升级为 BCrypt
            String md5 = DigestUtils.md5DigestAsHex(password.getBytes());
            if (md5.equals(stored)) {
                matched = true;
                // 自动升级密码为 BCrypt
                User update = new User();
                update.setId(user.getId());
                update.setPassword(passwordEncoder.encode(password));
                baseMapper.updateById(update);
                user.setPassword(update.getPassword());
            }
        }

        if (!matched) {
            return null;
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }
}
