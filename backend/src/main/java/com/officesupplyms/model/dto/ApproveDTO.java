package com.officesupplyms.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 审批DTO
 */
@Data
public class ApproveDTO {

    @NotNull(message = "申领单ID不能为空")
    private Long applicationId;

    @NotBlank(message = "审批操作不能为空")
    private String action; // APPROVE, REJECT

    private String rejectReason; // 驳回原因
}