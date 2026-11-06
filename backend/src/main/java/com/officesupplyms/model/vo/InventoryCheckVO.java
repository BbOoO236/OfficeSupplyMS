package com.officesupplyms.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 盘点VO
 */
@Data
public class InventoryCheckVO {

    private Long id;

    private String checkNo; // 盘点单号

    private Long materialId; // 物资ID
    private String materialName; // 物资名称
    private String materialCode; // 物资编码
    private String materialUnit; // 物资单位

    private Integer bookQuantity; // 账面数量

    private Integer actualQuantity; // 实际数量

    private Integer difference; // 差异数量

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkTime; // 盘点时间

    private Long operatorId; // 操作人ID
    private String operatorName; // 操作人姓名

    private String reason; // 差异原因

    private Integer adjustment; // 是否已调整: 0-未调整, 1-已调整

    private String remark;
}