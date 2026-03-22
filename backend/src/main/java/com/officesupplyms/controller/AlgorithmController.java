package com.officesupplyms.controller;

import com.officesupplyms.model.vo.AlgorithmResultVO;
import com.officesupplyms.service.AlgorithmService;
import com.officesupplyms.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 算法分析控制器
 */
@RestController
@RequestMapping("/algorithms")
public class AlgorithmController {

    @Autowired
    private AlgorithmService algorithmService;

    /**
     * ABC分类计算
     */
    @GetMapping("/abc-classification")
    public Result<AlgorithmResultVO.AbcClassificationResult> calculateAbcClassification() {
        return Result.success(algorithmService.calculateAbcClassification());
    }

    /**
     * 需求预测（移动平均法）
     */
    @GetMapping("/demand-forecast")
    public Result<List<AlgorithmResultVO.DemandForecastResult>> forecastDemand(
            @RequestParam(defaultValue = "3") Integer period) {
        return Result.success(algorithmService.forecastDemand(period));
    }

    /**
     * 对单个物资进行需求预测
     */
    @GetMapping("/demand-forecast/{materialId}")
    public Result<AlgorithmResultVO.DemandForecastResult> forecastMaterialDemand(
            @PathVariable Long materialId,
            @RequestParam(defaultValue = "3") Integer period) {
        return Result.success(algorithmService.forecastMaterialDemand(materialId, period));
    }

    /**
     * 补货点计算
     */
    @GetMapping("/reorder-points")
    public Result<List<AlgorithmResultVO.ReorderPointResult>> calculateReorderPoints() {
        return Result.success(algorithmService.calculateReorderPoints());
    }

    /**
     * 对单个物资进行补货点计算
     */
    @GetMapping("/reorder-points/{materialId}")
    public Result<AlgorithmResultVO.ReorderPointResult> calculateMaterialReorderPoint(
            @PathVariable Long materialId) {
        return Result.success(algorithmService.calculateMaterialReorderPoint(materialId));
    }

    /**
     * 异常检测（3σ原则）
     */
    @GetMapping("/anomaly-detection")
    public Result<List<AlgorithmResultVO.AnomalyDetectionResult>> detectAnomalies(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(algorithmService.detectAnomalies(date));
    }

    /**
     * 计算物资的日均消耗量
     */
    @GetMapping("/daily-consumption/{materialId}")
    public Result<java.math.BigDecimal> calculateDailyConsumption(
            @PathVariable Long materialId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(algorithmService.calculateDailyConsumption(materialId, startDate, endDate));
    }

    /**
     * 获取库存周转率数据
     */
    @GetMapping("/charts/inventory-turnover")
    public Result<List<AlgorithmResultVO.ChartData>> getInventoryTurnoverRate() {
        return Result.success(algorithmService.getInventoryTurnoverRate());
    }

    /**
     * 获取ABC分类占比数据
     */
    @GetMapping("/charts/abc-classification")
    public Result<AlgorithmResultVO.ChartData> getAbcClassificationChartData() {
        return Result.success(algorithmService.getAbcClassificationChartData());
    }

    /**
     * 获取每月申领单数量与金额趋势数据
     */
    @GetMapping("/charts/monthly-application-trend")
    public Result<AlgorithmResultVO.ChartData> getMonthlyApplicationTrend() {
        return Result.success(algorithmService.getMonthlyApplicationTrend());
    }

    /**
     * 获取预测与实际消耗对比数据
     */
    @GetMapping("/charts/forecast-vs-actual/{materialId}")
    public Result<AlgorithmResultVO.ChartData> getForecastVsActualComparison(
            @PathVariable Long materialId) {
        return Result.success(algorithmService.getForecastVsActualComparison(materialId));
    }

    /**
     * 手动触发ABC分类计算（定时任务）
     */
    @PostMapping("/schedule/abc-classification")
    public Result<Void> scheduleAbcClassification() {
        algorithmService.scheduleAbcClassification();
        return Result.success("ABC分类计算任务已触发");
    }

    /**
     * 手动触发补货点计算（定时任务）
     */
    @PostMapping("/schedule/reorder-point-calculation")
    public Result<Void> scheduleReorderPointCalculation() {
        algorithmService.scheduleReorderPointCalculation();
        return Result.success("补货点计算任务已触发");
    }

    /**
     * 手动触发需求预测（定时任务）
     */
    @PostMapping("/schedule/demand-forecast")
    public Result<Void> scheduleDemandForecast() {
        algorithmService.scheduleDemandForecast();
        return Result.success("需求预测任务已触发");
    }
}