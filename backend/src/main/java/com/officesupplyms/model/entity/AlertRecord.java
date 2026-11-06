package com.officesupplyms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预警记录实体类
 */
@Data
@TableName("alert_record")
public class AlertRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long materialId; // 物资ID

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
}