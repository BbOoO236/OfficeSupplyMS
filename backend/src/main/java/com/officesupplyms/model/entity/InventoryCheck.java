package com.officesupplyms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存盘点实体类
 */
@Data
@TableName("inventory_check")
public class InventoryCheck {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String checkNo; // 盘点单号

    private Long materialId; // 物资ID

    private Integer bookQuantity; // 账面数量

    private Integer actualQuantity; // 实际数量

    private Integer difference; // 差异数量

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkTime; // 盘点时间

    private Long operatorId; // 操作人ID

    private String reason; // 差异原因

    private Integer adjustment; // 是否已调整: 0-未调整, 1-已调整

    private String remark;
}