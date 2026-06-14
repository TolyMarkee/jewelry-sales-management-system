package com.jewelry.controller;

import com.jewelry.common.R;
import com.jewelry.entity.Product;
import com.jewelry.entity.StockRecord;
import com.jewelry.service.ProductService;
import com.jewelry.service.StockRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stock")
public class StockRecordController {

    @Autowired
    private StockRecordService stockRecordService;

    @Autowired
    private ProductService productService;

    /**
     * 库存记录列表
     */
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        Map<String, Object> result = stockRecordService.selectWithProduct(params);
        return R.ok().put("data", result);
    }

    /**
     * 入库
     */
    @PostMapping("/in")
    @Transactional
    public R stockIn(@RequestBody StockRecord record) {
        record.setType("in");
        stockRecordService.save(record);

        // 更新商品库存
        Product product = productService.getById(record.getProductId());
        if (product != null) {
            product.setStock(product.getStock() + record.getQuantity());
            productService.updateById(product);
        }
        return R.ok("入库成功");
    }

    /**
     * 出库
     */
    @PostMapping("/out")
    @Transactional
    public R stockOut(@RequestBody StockRecord record) {
        Product product = productService.getById(record.getProductId());
        if (product == null) {
            return R.error("商品不存在");
        }
        if (product.getStock() < record.getQuantity()) {
            return R.error("库存不足，当前库存：" + product.getStock());
        }

        record.setType("out");
        stockRecordService.save(record);

        product.setStock(product.getStock() - record.getQuantity());
        productService.updateById(product);
        return R.ok("出库成功");
    }
}
