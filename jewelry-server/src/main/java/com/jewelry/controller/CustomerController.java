package com.jewelry.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jewelry.common.R;
import com.jewelry.entity.Customer;
import com.jewelry.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    /**
     * 客户列表（分页 + 搜索）
     */
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        int page = Integer.parseInt(params.getOrDefault("page", "1").toString());
        int limit = Integer.parseInt(params.getOrDefault("limit", "10").toString());
        String keyword = (String) params.get("keyword");

        QueryWrapper<Customer> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("name", keyword).or().like("phone", keyword);
        }
        wrapper.orderByDesc("create_time");

        Page<Customer> pageResult = customerService.page(new Page<>(page, limit), wrapper);
        return R.ok().put("data", pageResult);
    }

    /**
     * 全部客户（下拉选择用）
     */
    @GetMapping("/all")
    public R all() {
        List<Customer> list = customerService.list();
        return R.ok().put("data", list);
    }

    /**
     * 新增客户
     */
    @PostMapping("/save")
    public R save(@RequestBody Customer customer) {
        customerService.save(customer);
        return R.ok("添加成功");
    }

    /**
     * 修改客户
     */
    @PutMapping("/update")
    public R update(@RequestBody Customer customer) {
        customerService.updateById(customer);
        return R.ok("修改成功");
    }

    /**
     * 删除客户
     */
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable Long id) {
        customerService.removeById(id);
        return R.ok("删除成功");
    }
}
