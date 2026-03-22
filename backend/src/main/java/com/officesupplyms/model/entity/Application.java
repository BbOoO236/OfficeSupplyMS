package com.officesupplyms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 申领单实体类
 */
@Data
@TableName("application")
public class Application {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String applicationNo; // 申领单号

    private Long userId; // 申请人ID

    private Long materialId; // 物资ID

    private Integer quantity; // 申领数量

    private String purpose; // 用途

    private String status; // PENDING, APPROVED, REJECTED, TO_BE_OUTBOUND, COMPLETED

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime; // 申请时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime; // 审批时间

    private Long approveUserId; // 审批人ID

    private String rejectReason; // 驳回原因

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime outboundTime; // 出库时间

    private Long outboundUserId; // 出库操作人ID

    private Integer isAbnormal; // 是否异常申领: 0-正常, 1-异常

    private String abnormalReason; // 异常原因

    private String remark;
}