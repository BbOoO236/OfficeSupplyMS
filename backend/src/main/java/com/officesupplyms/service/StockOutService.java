package com.officesupplyms.service;

import com.officesupplyms.model.dto.StockOutDTO;
import com.officesupplyms.model.vo.StockOutVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 出库服务接口
 */
public interface StockOutService {

    /**
     * 创建出库单
     */
    StockOutVO createStockOut(StockOutDTO stockOutDTO);

    /**
     * 根据ID查询出库单
     */
    StockOutVO getStockOutById(Long stockOutId);

    /**
     * 根据出库单号查询
     */
    StockOutVO getStockOutByNo(String stockOutNo);

    /**
     * 查询所有出库单
     */
    List<StockOutVO> getAllStockOuts();

    /**
     * 根据物资ID查询出库记录
     */
    List<StockOutVO> getStockOutsByMaterialId(Long materialId);

    /**
     * 根据出库类型查询出库记录
     */
    List<StockOutVO> getStockOutsByType(String type);

    /**
     * 根据申领单ID查询出库记录
     */
    StockOutVO getStockOutByApplicationId(Long applicationId);

    /**
     * 根据时间范围查询出库记录
     */
    List<StockOutVO> getStockOutsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询最近的出库记录
     */
    List<StockOutVO> getRecentStockOuts(Integer limit);

    /**
     * 统计指定物资的总出库数量
     */
    Integer sumQuantityByMaterialId(Long materialId);
}