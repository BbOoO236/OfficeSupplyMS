package com.officesupplyms.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 申领单VO
 */
@Data
public class ApplicationVO {

    private Long id;

    private String applicationNo; // 申领单号

    private Long userId; // 申请人ID
    private String userName; // 申请人姓名
    private String userDepartment; // 申请人部门

    private Long materialId; // 物资ID
    private String materialName; // 物资名称
    private String materialCode; // 物资编码
    private String materialUnit; // 物资单位

    private Integer quantity; // 申领数量

    private String purpose; // 用途

    private String status; // PENDING, APPROVED, REJECTED, TO_BE_OUTBOUND, COMPLETED

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime; // 申请时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime; // 审批时间

    private Long approveUserId; // 审批人ID
    private String approveUserName; // 审批人姓名

    private String rejectReason; // 驳回原因

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime outboundTime; // 出库时间

    private Long outboundUserId; // 出库操作人ID
    private String outboundUserName; // 出库操作人姓名

    private Integer isAbnormal; // 是否异常申领: 0-正常, 1-异常

    private String abnormalReason; // 异常原因

    private String remark;
}