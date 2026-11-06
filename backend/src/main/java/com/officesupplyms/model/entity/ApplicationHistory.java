package com.officesupplyms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 申领单操作历史记录
 */
@Data
@TableName("application_history")
public class ApplicationHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long applicationId; // 申领单ID

    private String action; // 动作：SUBMIT/APPROVE/REJECT/OUTBOUND/CANCEL/ROLLBACK

    private String fromStatus; // 原状态

    private String toStatus; // 新状态

    private Long operatorId; // 操作人ID

    private String operatorName; // 操作人姓名

    private String remark; // 备注/意见

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 操作时间
}
