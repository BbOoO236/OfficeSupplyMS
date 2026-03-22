package com.officesupplyms.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 历史消耗记录VO
 */
@Data
public class ConsumptionHistoryVO {

    private Long id;

    private Long materialId; // 物资ID

    private String materialName; // 物资名称

    private String materialCode; // 物资编码

    private Long userId; // 用户ID

    private String userName; // 用户姓名

    private Integer quantity; // 消耗数量

    private String type; // APPLICATION, OUTBOUND

    private LocalDate recordDate; // 记录日期

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordTime; // 记录时间

    private Long applicationId; // 关联申领单ID

    private String applicationNo; // 申领单号

    private Long stockOutId; // 关联出库单ID

    private String stockOutNo; // 出库单号
}