package com.jewelry.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jewelry.entity.Product;

import java.util.List;
import java.util.Map;

public interface ProductService extends IService<Product> {

    /**
     * 分页查询商品（含分类名称）
     */
    Map<String, Object> selectWithCategory(Map<String, Object> params);
}
