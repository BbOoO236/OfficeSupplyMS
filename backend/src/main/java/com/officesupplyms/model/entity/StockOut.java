package com.officesupplyms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 出库单实体类
 */
@Data
@TableName("stock_out")
public class StockOut {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String stockOutNo; // 出库单号

    private Long materialId; // 物资ID

    private Integer quantity; // 出库数量

    private String type; // APPLICATION, DIRECT, SCRAP

    private Long applicationId; // 关联申领单ID

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime outTime; // 出库时间

    private Long operatorId; // 操作人ID

    private String reason; // 出库原因

    private String remark;
}