package com.officesupplyms.service;

import com.officesupplyms.model.vo.ConsumptionHistoryVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 历史消耗记录服务接口
 */
public interface ConsumptionHistoryService {

    /**
     * 根据ID查询消耗记录
     */
    ConsumptionHistoryVO getById(Long id);

    /**
     * 查询所有消耗记录
     */
    List<ConsumptionHistoryVO> getAll();

    /**
     * 根据物资ID查询消耗记录
     */
    List<ConsumptionHistoryVO> getByMaterialId(Long materialId);

    /**
     * 根据用户ID查询消耗记录
     */
    List<ConsumptionHistoryVO> getByUserId(Long userId);

    /**
     * 根据时间范围查询消耗记录
     */
    List<ConsumptionHistoryVO> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据记录日期查询消耗记录
     */
    List<ConsumptionHistoryVO> getByRecordDate(LocalDate recordDate);

    /**
     * 根据类型查询消耗记录
     */
    List<ConsumptionHistoryVO> getByType(String type);

    /**
     * 查询最近的消耗记录
     */
    List<ConsumptionHistoryVO> getRecent(Integer limit);

    /**
     * 统计指定物资的总消耗数量
     */
    Integer sumQuantityByMaterialId(Long materialId);

    /**
     * 统计指定用户的总消耗数量
     */
    Integer sumQuantityByUserId(Long userId);

    /**
     * 创建消耗记录（内部调用）
     */
    boolean createConsumptionHistory(Long materialId, Long userId, Integer quantity,
                                     String type, Long applicationId, Long stockOutId);
}