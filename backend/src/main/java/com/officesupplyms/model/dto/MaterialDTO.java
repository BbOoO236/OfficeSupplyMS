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

    @NotNull(message = "采购提前期不能为空")
    private Integer leadTime = 7;

    @NotNull(message = "安全系数不能为空")
    private BigDecimal safetyFactor = new BigDecimal("1.5");

    @NotNull(message = "当前库存不能为空")
    private Integer currentStock = 0;

    @NotNull(message = "最低库存不能为空")
    private Integer minStock = 0;

    @NotNull(message = "最高库存不能为空")
    private Integer maxStock = 0;

    private Integer status = 1; // 1-正常, 0-停用

    private String remark;
}