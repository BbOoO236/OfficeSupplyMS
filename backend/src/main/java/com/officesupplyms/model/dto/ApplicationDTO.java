package com.officesupplyms.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 申领创建DTO
 */
@Data
public class ApplicationDTO {

    @NotNull(message = "物资ID不能为空")
    private Long materialId;

    @NotNull(message = "申领数量不能为空")
    private Integer quantity;

    @NotBlank(message = "用途不能为空")
    private String purpose;

    private String remark;
}