package com.officesupplyms.service;

import com.officesupplyms.model.dto.InventoryCheckDTO;
import com.officesupplyms.model.vo.InventoryCheckVO;
import com.officesupplyms.model.vo.MaterialCheckSummaryVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存盘点服务接口
 */
public interface InventoryCheckService {

    /**
     * 创建盘点记录
     */
    InventoryCheckVO createInventoryCheck(InventoryCheckDTO inventoryCheckDTO);

    /**
     * 根据ID查询盘点记录
     */
    InventoryCheckVO getInventoryCheckById(Long checkId);

    /**
     * 根据盘点单号查询
     */
    InventoryCheckVO getInventoryCheckByNo(String checkNo);

    /**
     * 查询所有盘点记录
     */
    List<InventoryCheckVO> getAllInventoryChecks();

    /**
     * 根据物资ID查询盘点记录
     */
    List<InventoryCheckVO> getInventoryChecksByMaterialId(Long materialId);

    /**
     * 根据时间范围查询盘点记录
     */
    List<InventoryCheckVO> getInventoryChecksByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询未调整的盘点记录
     */
    List<InventoryCheckVO> getUnadjustedInventoryChecks();

    /**
     * 查询有差异的盘点记录
     */
    List<InventoryCheckVO> getInventoryChecksWithDifference();

    /**
     * 更新盘点记录状态为已调整
     */
    boolean markAsAdjusted(Long checkId);

    /**
     * 调整库存根据盘点差异
     */
    boolean adjustStockByCheck(Long checkId, Integer actualQuantity, String adjustmentRemark);

    /**
     * 查询最近的盘点记录
     */
    List<InventoryCheckVO> getRecentInventoryChecks(Integer limit);

    /**
     * 统计指定物资的盘点差异总量
     */
    Integer sumDifferenceByMaterialId(Long materialId);

    /**
     * 获取所有物资的最新盘点汇总列表
     */
    List<MaterialCheckSummaryVO> getMaterialCheckSummaryList();

    /**
     * 获取指定物资的盘点汇总
     */
    MaterialCheckSummaryVO getMaterialCheckSummary(Long materialId);

    /**
     * 库存变化时自动更新盘点记录
     * 账面数量更新为新库存，实际数量不变，计算差异
     * 如果没有盘点记录则自动创建一个
     * @param materialId 物资ID
     * @param oldStock 变化前的库存
     * @param newStock 变化后的库存
     */
    void updateCheckOnStockChange(Long materialId, Integer oldStock, Integer newStock);
}