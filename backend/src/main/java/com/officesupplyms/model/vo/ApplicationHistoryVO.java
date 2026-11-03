package com.officesupplyms.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 申领单操作历史VO
 */
@Data
public class ApplicationHistoryVO {

    private Long id;

    private String action; // 动作

    private String actionText; // 动作中文描述

    private String fromStatus; // 原状态

    private String toStatus; // 新状态

    private String operatorName; // 操作人

    private String remark; // 备注

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 操作时间
}
