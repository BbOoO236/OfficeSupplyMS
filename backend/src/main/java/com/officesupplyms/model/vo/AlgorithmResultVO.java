package com.officesupplyms.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 算法结果VO
 */
@Data
public class AlgorithmResultVO {

    // ABC分类结果
    @Data
    public static class AbcClassificationResult {
        private List<MaterialVO> classA; // A类物资
        private List<MaterialVO> classB; // B类物资
        private List<MaterialVO> classC; // C类物资
        private Map<String, BigDecimal> percentages; // 各类占比
        private String calculationTime; // 计算时间
    }

    // 需求预测结果
    @Data
    public static class DemandForecastResult {
        private Long materialId;
        private String materialName;
        private String materialCode;
        private List<HistoricalData> historicalData; // 历史数据
        private BigDecimal simpleMovingAverage; // 简单移动平均预测值
        private BigDecimal weightedMovingAverage; // 加权移动平均预测值
        private BigDecimal actualConsumption; // 实际消耗量（如果有）
        private String forecastDate; // 预测日期
    }

    @Data
    public static class HistoricalData {
        private String date;
        private BigDecimal consumption;
    }

    // 补货点计算结果
    @Data
    public static class ReorderPointResult {
        private Long materialId;
        private String materialName;
        private String materialCode;
        private Integer currentStock; // 当前库存
        private BigDecimal dailyConsumption; // 日均消耗量
        private Integer leadTime; // 采购提前期
        private BigDecimal safetyFactor; // 安全系数
        private Integer safetyStock; // 安全库存
        private Integer reorderPoint; // 补货点
        private Boolean needReorder; // 是否需要补货
        private String calculationTime; // 计算时间
    }

    // 异常检测结果
    @Data
    public static class AnomalyDetectionResult {
        private Long applicationId;
        private String applicationNo;
        private Long userId;
        private String userName;
        private Long materialId;
        private String materialName;
        private Integer currentQuantity; // 本次申领数量
        private BigDecimal historicalMean; // 历史均值
        private BigDecimal historicalStdDev; // 历史标准差
        private BigDecimal threshold; // 阈值 (μ + 3σ)
        private Boolean isAnomaly; // 是否异常
        private String detectionTime; // 检测时间
    }

    // 数据可视化VO
    @Data
    public static class ChartData {
        private List<String> labels; // X轴标签
        private List<BigDecimal> values; // 数据值
        private String title; // 图表标题
        private String unit; // 单位
    }
}