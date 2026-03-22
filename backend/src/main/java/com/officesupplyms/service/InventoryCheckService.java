package com.officesupplyms.service;

import com.officesupplyms.model.dto.InventoryCheckDTO;
import com.officesupplyms.model.vo.InventoryCheckVO;

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
    boolean adjustStockByCheck(Long checkId);

    /**
     * 查询最近的盘点记录
     */
    List<InventoryCheckVO> getRecentInventoryChecks(Integer limit);

    /**
     * 统计指定物资的盘点差异总量
     */
    Integer sumDifferenceByMaterialId(Long materialId);
}