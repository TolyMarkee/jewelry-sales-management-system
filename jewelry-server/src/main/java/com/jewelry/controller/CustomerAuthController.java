package com.jewelry.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jewelry.common.JwtUtil;
import com.jewelry.common.R;
import com.jewelry.entity.Customer;
import com.jewelry.mapper.CustomerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/shop")
public class CustomerAuthController {

    @Autowired private CustomerMapper customerMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    /** 客户注册 */
    @PostMapping("/register")
    public R register(@RequestBody Map<String, String> params) {
        String name = params.get("name"); String phone = params.get("phone");
        String password = params.get("password"); String email = params.getOrDefault("email","");
        String address = params.getOrDefault("address","");
        if (name == null || phone == null || password == null) return R.error("请填写完整信息");
        if (password.length() < 6) return R.error("密码至少6位");
        Long exists = customerMapper.selectCount(new QueryWrapper<Customer>().eq("phone", phone));
        if (exists > 0) return R.error("该手机号已注册");
        Customer c = new Customer(); c.setName(name); c.setPhone(phone);
        c.setEmail(email); c.setAddress(address);
        c.setPassword(passwordEncoder.encode(password));
        customerMapper.insert(c);
        return R.ok("注册成功");
    }

    /** 客户登录 */
    @PostMapping("/login")
    public R login(@RequestParam String phone, @RequestParam String password) {
        Customer c = customerMapper.selectOne(new QueryWrapper<Customer>().eq("phone", phone));
        if (c == null || !passwordEncoder.matches(password, c.getPassword())) return R.error("手机号或密码错误");
        String token = jwtUtil.generateToken(c.getId(), c.getPhone());
        Map<String, Object> data = new HashMap<>(); data.put("token", token); data.put("customer", c);
        return R.ok().data(data);
    }

    /** 客户信息 */
    @GetMapping("/profile")
    public R profile(@RequestHeader("Authorization") String auth) {
        try { String token = auth.replace("Bearer ",""); Long id = jwtUtil.getUserIdFromToken(token);
            return R.ok().data(customerMapper.selectById(id)); } catch (Exception e) { return R.error("请先登录"); }
    }
}
