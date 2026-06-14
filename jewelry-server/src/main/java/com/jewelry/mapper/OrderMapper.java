package com.jewelry.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.jewelry.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT o.*, c.name AS customer_name, u.real_name AS user_name " +
            "FROM `order` o " +
            "LEFT JOIN customer c ON o.customer_id = c.id " +
            "LEFT JOIN `user` u ON o.user_id = u.id " +
            "${ew.customSqlSegment}")
    IPage<Order> selectWithRelations(IPage<Order> page, @Param(Constants.WRAPPER) Wrapper<Order> wrapper);
}
