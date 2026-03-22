package com.officesupplyms.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入库单VO
 */
@Data
public class StockInVO {

    private Long id;

    private String stockInNo; // 入库单号

    private Long materialId; // 物资ID
    private String materialName; // 物资名称
    private String materialCode; // 物资编码
    private String materialUnit; // 物资单位

    private Integer quantity; // 入库数量

    private BigDecimal unitPrice; // 单价

    private BigDecimal totalAmount; // 总金额

    private String supplier; // 供应商

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime inTime; // 入库时间

    private Long operatorId; // 操作人ID
    private String operatorName; // 操作人姓名

    private String remark;
}