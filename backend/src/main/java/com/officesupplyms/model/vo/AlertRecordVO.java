package com.officesupplyms.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预警记录VO
 */
@Data
public class AlertRecordVO {

    private Long id;

    private Long materialId; // 物资ID
    private String materialName; // 物资名称
    private String materialCode; // 物资编码

    private String alertType; // LOW_STOCK, ABNORMAL_APPLICATION

    private String alertLevel; // HIGH, MEDIUM, LOW

    private BigDecimal currentValue; // 当前值

    private BigDecimal threshold; // 阈值

    private String message; // 预警信息

    private String status; // UNREAD, READ, PROCESSED

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime alertTime; // 预警时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime; // 读取时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processTime; // 处理时间

    private Long processUserId; // 处理人ID
    private String processUserName; // 处理人姓名
}