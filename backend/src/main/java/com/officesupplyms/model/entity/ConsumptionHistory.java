package com.officesupplyms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 历史消耗记录实体类
 */
@Data
@TableName("consumption_history")
public class ConsumptionHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long materialId; // 物资ID

    private Long userId; // 用户ID

    private Integer quantity; // 消耗数量

    private String type; // APPLICATION, OUTBOUND

    private LocalDate recordDate; // 记录日期

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordTime; // 记录时间

    private Long applicationId; // 关联申领单ID

    private Long stockOutId; // 关联出库单ID
}