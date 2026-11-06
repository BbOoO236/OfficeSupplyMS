package com.officesupplyms.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 预警记录DTO
 */
@Data
public class AlertRecordDTO {

    @NotNull(message = "物资ID不能为空")
    private Long materialId;

    @NotBlank(message = "预警类型不能为空")
    private String alertType; // LOW_STOCK, ABNORMAL_APPLICATION

    @NotBlank(message = "预警等级不能为空")
    private String alertLevel; // HIGH, MEDIUM, LOW

    @NotNull(message = "当前值不能为空")
    private BigDecimal currentValue;

    @NotNull(message = "阈值不能为空")
    private BigDecimal threshold;

    @NotBlank(message = "预警信息不能为空")
    private String message;
}