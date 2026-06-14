package com.jewelry.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jewelry.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {

    /**
     * 登录验证
     */
    Map<String, Object> login(String username, String password);
}
