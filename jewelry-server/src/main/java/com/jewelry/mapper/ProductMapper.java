package com.jewelry.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.jewelry.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Select("SELECT p.*, c.name AS category_name FROM product p " +
            "LEFT JOIN category c ON p.category_id = c.id " +
            "${ew.customSqlSegment}")
    IPage<Product> selectWithCategory(IPage<Product> page, @Param(Constants.WRAPPER) Wrapper<Product> wrapper);
}
