package com.officesupplyms.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 物资创建/更新DTO
 */
@Data
public class MaterialDTO {

    private Long id;

    @NotBlank(message = "物资编码不能为空")
    private String code;

    @NotBlank(message = "物资名称不能为空")
    private String name;

    @NotBlank(message = "物资类别不能为空")
    private String category;

    private String specification;

    @NotBlank(message = "单位不能为空")
    private String unit;

    @NotNull(message = "单价不能为空")
    private BigDecimal unitPrice;

    private Integer leadTime;

    private BigDecimal safetyFactor;

    private Integer currentStock = 0;

    private Integer minStock;

    private Integer maxStock;

    private Integer status = 1; // 1-正常, 0-停用

    private String remark;
}