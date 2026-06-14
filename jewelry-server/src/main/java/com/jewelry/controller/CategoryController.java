package com.jewelry.controller;

import com.jewelry.common.R;
import com.jewelry.entity.Category;
import com.jewelry.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 分类列表
     */
    @GetMapping("/list")
    public R list() {
        List<Category> list = categoryService.list();
        return R.ok().put("data", list);
    }

    /**
     * 新增分类
     */
    @PostMapping("/save")
    public R save(@RequestBody Category category) {
        categoryService.save(category);
        return R.ok("添加成功");
    }

    /**
     * 修改分类
     */
    @PutMapping("/update")
    public R update(@RequestBody Category category) {
        categoryService.updateById(category);
        return R.ok("修改成功");
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable Long id) {
        categoryService.removeById(id);
        return R.ok("删除成功");
    }
}
