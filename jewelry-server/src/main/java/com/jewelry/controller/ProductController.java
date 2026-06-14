package com.jewelry.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jewelry.common.R;
import com.jewelry.entity.Product;
import com.jewelry.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 商品列表（分页 + 搜索 + 含分类名）
     */
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        Map<String, Object> result = productService.selectWithCategory(params);
        return R.ok().put("data", result);
    }

    /**
     * 获取全部商品（下拉选择用）
     */
    @GetMapping("/all")
    public R all() {
        List<Product> list = productService.list();
        return R.ok().put("data", list);
    }

    /**
     * 新增商品
     */
    @PostMapping("/save")
    public R save(@RequestBody Product product) {
        productService.save(product);
        return R.ok("添加成功");
    }

    /**
     * 修改商品
     */
    @PutMapping("/update")
    public R update(@RequestBody Product product) {
        productService.updateById(product);
        return R.ok("修改成功");
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable Long id) {
        productService.removeById(id);
        return R.ok("删除成功");
    }
}
