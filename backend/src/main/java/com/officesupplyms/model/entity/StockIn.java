package com.officesupplyms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入库单实体类
 */
@Data
@TableName("stock_in")
public class StockIn {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String stockInNo; // 入库单号

    private Long materialId; // 物资ID

    private Integer quantity; // 入库数量

    private BigDecimal unitPrice; // 单价

    private BigDecimal totalAmount; // 总金额

    private String supplier; // 供应商

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime inTime; // 入库时间

    private Long operatorId; // 操作人ID

    private String remark;
}