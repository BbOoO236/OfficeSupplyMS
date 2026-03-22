package com.officesupplyms.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 出库单VO
 */
@Data
public class StockOutVO {

    private Long id;

    private String stockOutNo; // 出库单号

    private Long materialId; // 物资ID
    private String materialName; // 物资名称
    private String materialCode; // 物资编码
    private String materialUnit; // 物资单位

    private Integer quantity; // 出库数量

    private String type; // APPLICATION, DIRECT, SCRAP

    private Long applicationId; // 关联申领单ID
    private String applicationNo; // 申领单号

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime outTime; // 出库时间

    private Long operatorId; // 操作人ID
    private String operatorName; // 操作人姓名

    private String reason; // 出库原因

    private String remark;
}