package com.jewelry.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jewelry.entity.Product;
import com.jewelry.mapper.ProductMapper;
import com.jewelry.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Override
    public Map<String, Object> selectWithCategory(Map<String, Object> params) {
        int pageNum = Integer.parseInt(params.getOrDefault("page", "1").toString());
        int limit = Integer.parseInt(params.getOrDefault("limit", "10").toString());
        String keyword = params.getOrDefault("keyword", "").toString();
        Long categoryId = params.get("categoryId") != null && !params.get("categoryId").toString().isEmpty()
                ? Long.parseLong(params.get("categoryId").toString()) : null;

        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("p.name", keyword);
        }
        if (categoryId != null) {
            wrapper.eq("p.category_id", categoryId);
        }
        wrapper.orderByDesc("p.create_time");

        Page<Product> page = new Page<>(pageNum, limit);
        IPage<Product> result = baseMapper.selectWithCategory(page, wrapper);

        Map<String, Object> map = new HashMap<>();
        map.put("total", result.getTotal());
        map.put("list", result.getRecords());
        return map;
    }
}
