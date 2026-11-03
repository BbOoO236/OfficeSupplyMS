package com.officesupplyms.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物资盘点汇总VO
 */
@Data
public class MaterialCheckSummaryVO {

    private Long materialId; // 物资ID
    private String materialName; // 物资名称
    private String materialCode; // 物资编码
    private String materialUnit; // 物资单位

    // 最新盘点信息
    private Long lastCheckId; // 最新盘点记录ID
    private String lastCheckNo; // 最新盘点单号
    private Integer bookQuantity; // 账面数量
    private Integer actualQuantity; // 实际数量
    private Integer difference; // 差异数量
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkTime; // 盘点时间
    private Integer adjustment; // 是否已调整: 0-未调整, 1-已调整
    private String remark; // 备注

    // 历史盘点记录（全部有差异记录 + 最后一条无差异记录）
    private List<InventoryCheckVO> historyList;
}
