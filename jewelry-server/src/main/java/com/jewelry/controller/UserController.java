package com.jewelry.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jewelry.common.R;
import com.jewelry.entity.User;
import com.jewelry.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户列表（管理员）
     */
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        String keyword = (String) params.get("keyword");
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("username", keyword).or().like("real_name", keyword);
        }
        wrapper.orderByDesc("create_time");
        List<User> list = userService.list(wrapper);
        return R.ok().put("data", list);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/profile")
    public R profile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        return R.ok().put("data", user);
    }

    /**
     * 更新个人资料（姓名、电话、邮箱、头像）
     */
    @PutMapping("/profile")
    public R updateProfile(@RequestBody User user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setRealName(user.getRealName());
        updateUser.setPhone(user.getPhone());
        updateUser.setEmail(user.getEmail());
        updateUser.setAvatar(user.getAvatar());
        userService.updateById(updateUser);

        // 返回更新后的用户信息
        User fresh = userService.getById(userId);
        return R.ok("资料更新成功").put("data", fresh);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public R changePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return R.error("参数不完整");
        }

        User user = userService.getById(userId);
        String oldMd5 = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if (!oldMd5.equals(user.getPassword())) {
            return R.error("原密码错误");
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        userService.updateById(updateUser);

        return R.ok("密码修改成功，请重新登录");
    }

    /**
     * 新增用户
     */
    @PostMapping("/save")
    public R save(@RequestBody User user) {
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        userService.save(user);
        return R.ok("添加成功");
    }

    /**
     * 修改用户
     */
    @PutMapping("/update")
    public R update(@RequestBody User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        } else {
            user.setPassword(null);
        }
        userService.updateById(user);
        return R.ok("修改成功");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable Long id) {
        userService.removeById(id);
        return R.ok("删除成功");
    }
}
