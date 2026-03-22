package com.officesupplyms.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 入库DTO
 */
@Data
public class StockInDTO {

    @NotNull(message = "物资ID不能为空")
    private Long materialId;

    @NotNull(message = "入库数量不能为空")
    private Integer quantity;

    @NotNull(message = "单价不能为空")
    private BigDecimal unitPrice;

    private String supplier;

    private String remark;
}