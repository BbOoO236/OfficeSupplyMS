package com.officesupplyms.service;

import com.officesupplyms.model.vo.AlgorithmResultVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 算法服务接口
 */
public interface AlgorithmService {

    /**
     * ABC分类计算
     */
    AlgorithmResultVO.AbcClassificationResult calculateAbcClassification();

    /**
     * 需求预测（移动平均法）
     */
    List<AlgorithmResultVO.DemandForecastResult> forecastDemand(Integer period);

    /**
     * 对单个物资进行需求预测
     */
    AlgorithmResultVO.DemandForecastResult forecastMaterialDemand(Long materialId, Integer period);

    /**
     * 补货点计算
     */
    List<AlgorithmResultVO.ReorderPointResult> calculateReorderPoints();

    /**
     * 对单个物资进行补货点计算
     */
    AlgorithmResultVO.ReorderPointResult calculateMaterialReorderPoint(Long materialId);

    /**
     * 异常检测（3σ原则）
     */
    List<AlgorithmResultVO.AnomalyDetectionResult> detectAnomalies(LocalDate date);

    /**
     * 计算物资的日均消耗量
     */
    java.math.BigDecimal calculateDailyConsumption(Long materialId, LocalDate startDate, LocalDate endDate);

    /**
     * 计算物资的历史申领统计（均值和标准差）
     */
    AlgorithmResultVO.AnomalyDetectionResult calculateUserMaterialStatistics(Long userId, Long materialId);

    /**
     * 更新物资的年消耗数据
     */
    void updateMaterialAnnualConsumption();

    /**
     * 执行定时任务：ABC分类计算
     */
    void scheduleAbcClassification();

    /**
     * 执行定时任务：补货点计算
     */
    void scheduleReorderPointCalculation();

    /**
     * 执行定时任务：需求预测
     */
    void scheduleDemandForecast();

    /**
     * 获取库存周转率数据
     */
    List<AlgorithmResultVO.ChartData> getInventoryTurnoverRate();

    /**
     * 获取ABC分类占比数据
     */
    AlgorithmResultVO.ChartData getAbcClassificationChartData();

    /**
     * 获取每月申领单数量与金额趋势数据
     */
    AlgorithmResultVO.ChartData getMonthlyApplicationTrend();

    /**
     * 获取预测与实际消耗对比数据
     */
    AlgorithmResultVO.ChartData getForecastVsActualComparison(Long materialId);
}