package com.jewelry.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jewelry.entity.StockRecord;

import java.util.Map;

public interface StockRecordService extends IService<StockRecord> {

    /**
     * 分页查询库存记录（含商品名称）
     */
    Map<String, Object> selectWithProduct(Map<String, Object> params);
}
