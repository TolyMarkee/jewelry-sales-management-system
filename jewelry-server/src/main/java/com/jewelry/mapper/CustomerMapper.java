package com.jewelry.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jewelry.entity.Customer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
}
