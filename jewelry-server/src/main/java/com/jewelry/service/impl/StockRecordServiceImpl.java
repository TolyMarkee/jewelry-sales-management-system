package com.jewelry.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jewelry.entity.StockRecord;
import com.jewelry.mapper.StockRecordMapper;
import com.jewelry.service.StockRecordService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StockRecordServiceImpl extends ServiceImpl<StockRecordMapper, StockRecord> implements StockRecordService {

    @Override
    public Map<String, Object> selectWithProduct(Map<String, Object> params) {
        int pageNum = Integer.parseInt(params.getOrDefault("page", "1").toString());
        int limit = Integer.parseInt(params.getOrDefault("limit", "10").toString());

        QueryWrapper<StockRecord> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("s.create_time");

        Page<StockRecord> page = new Page<>(pageNum, limit);
        IPage<StockRecord> result = baseMapper.selectWithProduct(page, wrapper);

        Map<String, Object> map = new HashMap<>();
        map.put("total", result.getTotal());
        map.put("list", result.getRecords());
        return map;
    }
}
