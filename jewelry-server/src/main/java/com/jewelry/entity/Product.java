package com.jewelry.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long categoryId;
    private String material;
    private BigDecimal price;
    private Integer stock;
    private String image;
    private String description;
    private Date createTime;

    // 用于关联查询，非数据库字段
    private transient String categoryName;
}
