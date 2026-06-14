package com.jewelry.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@TableName("`order`")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long customerId;
    private Long userId;
    private BigDecimal totalAmount;
    private String status;
    private String remark;
    private Date createTime;

    // 用于关联查询，非数据库字段
    @TableField(exist = false)
    private String customerName;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private List<OrderItem> items;
}
