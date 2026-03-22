package com.officesupplyms.service;

import com.officesupplyms.model.dto.StockInDTO;
import com.officesupplyms.model.vo.StockInVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 入库服务接口
 */
public interface StockInService {

    /**
     * 创建入库单
     */
    StockInVO createStockIn(StockInDTO stockInDTO);

    /**
     * 根据ID查询入库单
     */
    StockInVO getStockInById(Long stockInId);

    /**
     * 根据入库单号查询
     */
    StockInVO getStockInByNo(String stockInNo);

    /**
     * 查询所有入库单
     */
    List<StockInVO> getAllStockIns();

    /**
     * 根据物资ID查询入库记录
     */
    List<StockInVO> getStockInsByMaterialId(Long materialId);

    /**
     * 根据时间范围查询入库记录
     */
    List<StockInVO> getStockInsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询最近的入库记录
     */
    List<StockInVO> getRecentStockIns(Integer limit);

    /**
     * 统计指定物资的总入库数量
     */
    Integer sumQuantityByMaterialId(Long materialId);

    /**
     * 统计指定物资的总入库金额
     */
    java.math.BigDecimal sumAmountByMaterialId(Long materialId);
}