package com.officesupplyms.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 盘点DTO
 */
@Data
public class InventoryCheckDTO {

    @NotNull(message = "物资ID不能为空")
    private Long materialId;

    @NotNull(message = "实际数量不能为空")
    private Integer actualQuantity;

    private String reason; // 差异原因

    private String remark;
}