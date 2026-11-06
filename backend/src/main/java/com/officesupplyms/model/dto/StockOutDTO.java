package com.officesupplyms.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 出库DTO
 */
@Data
public class StockOutDTO {

    @NotNull(message = "物资ID不能为空")
    private Long materialId;

    @NotNull(message = "出库数量不能为空")
    private Integer quantity;

    @NotBlank(message = "出库类型不能为空")
    private String type; // APPLICATION, DIRECT, SCRAP

    private Long applicationId; // 关联申领单ID

    private String reason; // 出库原因

    private String remark;
}