package com.jewelry.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.jewelry.entity.StockRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StockRecordMapper extends BaseMapper<StockRecord> {

    @Select("SELECT s.*, p.name AS product_name FROM stock_record s " +
            "LEFT JOIN product p ON s.product_id = p.id " +
            "${ew.customSqlSegment}")
    IPage<StockRecord> selectWithProduct(IPage<StockRecord> page, @Param(Constants.WRAPPER) Wrapper<StockRecord> wrapper);
}
