package com.officesupplyms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 入库单操作历史记录
 */
@Data
@TableName("stock_in_history")
public class StockInHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long stockInId; // 入库单ID

    private String action; // 动作：CREATE/UPDATE/DELETE

    private Long operatorId; // 操作人ID

    private String operatorName; // 操作人姓名

    private String remark; // 备注

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 操作时间
}
