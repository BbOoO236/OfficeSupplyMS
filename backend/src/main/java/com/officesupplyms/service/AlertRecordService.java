package com.officesupplyms.service;

import com.officesupplyms.model.vo.AlertRecordVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预警记录服务接口
 */
public interface AlertRecordService {

    /**
     * 创建预警记录
     */
    boolean createAlertRecord(Long materialId, String alertType, String alertLevel,
                              java.math.BigDecimal currentValue, java.math.BigDecimal threshold,
                              String message);

    /**
     * 根据ID查询预警记录
     */
    AlertRecordVO getAlertRecordById(Long alertId);

    /**
     * 查询所有预警记录
     */
    List<AlertRecordVO> getAllAlertRecords();

    /**
     * 根据预警类型查询
     */
    List<AlertRecordVO> getAlertRecordsByType(String alertType);

    /**
     * 根据预警等级查询
     */
    List<AlertRecordVO> getAlertRecordsByLevel(String alertLevel);

    /**
     * 根据状态查询预警记录
     */
    List<AlertRecordVO> getAlertRecordsByStatus(String status);

    /**
     * 根据物资ID查询预警记录
     */
    List<AlertRecordVO> getAlertRecordsByMaterialId(Long materialId);

    /**
     * 根据时间范围查询预警记录
     */
    List<AlertRecordVO> getAlertRecordsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询未读预警记录
     */
    List<AlertRecordVO> getUnreadAlertRecords();

    /**
     * 标记预警为已读
     */
    boolean markAsRead(Long alertId);

    /**
     * 标记预警为已处理
     */
    boolean markAsProcessed(Long alertId, Long processUserId);

    /**
     * 统计未读预警数量
     */
    Integer countUnreadAlertRecords();

    /**
     * 查询最近的预警记录
     */
    List<AlertRecordVO> getRecentAlertRecords(Integer limit);
}