package com.jewelry.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jewelry.entity.Category;
import com.jewelry.mapper.CategoryMapper;
import com.jewelry.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
}
