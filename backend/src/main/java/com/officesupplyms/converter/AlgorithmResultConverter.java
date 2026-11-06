package com.officesupplyms.converter;

import com.officesupplyms.model.entity.Material;
import com.officesupplyms.model.vo.AlgorithmResultVO;
import com.officesupplyms.model.vo.AlgorithmResultVO.*;
import com.officesupplyms.model.vo.MaterialVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 算法结果转换器
 * 将后端嵌套结构转换为前端扁平结构
 */
public class AlgorithmResultConverter {

    private AlgorithmResultConverter() {
        // 工具类禁止实例化
    }

    /**
     * ABC分类结果扁平化
     * 将 classA/classB/classC 列表合并为扁平列表
     */
    public static List<AbcClassificationFlatVO> convertAbcClassification(AbcClassificationResult result) {
        if (result == null) {
            return Collections.emptyList();
        }

        List<AbcClassificationFlatVO> flatList = new ArrayList<>();

        // 合并所有分类的物资并计算总金额
        List<MaterialVO> allMaterials = new ArrayList<>();
        allMaterials.addAll(result.getClassA() != null ? result.getClassA() : Collections.emptyList());
        allMaterials.addAll(result.getClassB() != null ? result.getClassB() : Collections.emptyList());
        allMaterials.addAll(result.getClassC() != null ? result.getClassC() : Collections.emptyList());

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (MaterialVO material : allMaterials) {
            if (material.getAnnualConsumptionAmount() != null) {
                totalAmount = totalAmount.add(material.getAnnualConsumptionAmount());
            }
        }

        // 计算累计百分比并填充扁平化结果
        BigDecimal cumulativeAmount = BigDecimal.ZERO;
        for (MaterialVO material : allMaterials) {
            AbcClassificationFlatVO flatVO = new AbcClassificationFlatVO();
            flatVO.setMaterialId(material.getId());
            flatVO.setMaterialName(material.getName());
            flatVO.setMaterialCode(material.getCode());
            flatVO.setAnnualConsumptionAmount(material.getAnnualConsumptionAmount());
            flatVO.setConsumptionAmount(material.getAnnualConsumptionAmount()); // 前端展示用

            // 计算累计百分比
            if (material.getAnnualConsumptionAmount() != null && totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                cumulativeAmount = cumulativeAmount.add(material.getAnnualConsumptionAmount());
                BigDecimal cumulativePercentage = cumulativeAmount.divide(totalAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                flatVO.setCumulativePercentage(cumulativePercentage);
            } else {
                flatVO.setCumulativePercentage(BigDecimal.ZERO);
            }

            // 设置ABC分类和管理策略
            flatVO.setAbcClass(material.getAbcClass());
            flatVO.setManagementStrategy(getManagementStrategy(material.getAbcClass()));

            flatList.add(flatVO);
        }

        return flatList;
    }

    /**
     * 根据ABC分类获取管理策略
     */
    private static String getManagementStrategy(String abcClass) {
        if (abcClass == null) {
            return "简化管理，按需采购";
        }
        switch (abcClass) {
            case "A":
                return "重点管理，优先补货";
            case "B":
                return "常规管理，定期盘点";
            case "C":
            default:
                return "简化管理，按需采购";
        }
    }

    /**
     * 需求预测结果扁平化
     */
    public static DemandForecastFlatVO convertDemandForecast(DemandForecastResult result) {
        if (result == null) {
            return null;
        }

        DemandForecastFlatVO flatVO = new DemandForecastFlatVO();
        flatVO.setMaterialId(result.getMaterialId());
        flatVO.setMaterialName(result.getMaterialName());
        flatVO.setMaterialCode(result.getMaterialCode());

        // historicalAvg = simpleMovingAverage
        flatVO.setHistoricalAvg(result.getSimpleMovingAverage());

        // forecastAvg = weightedMovingAverage
        flatVO.setForecastAvg(result.getWeightedMovingAverage());

        // forecastTotal = forecastAvg * 3
        if (result.getWeightedMovingAverage() != null) {
            flatVO.setForecastTotal(result.getWeightedMovingAverage().multiply(new BigDecimal("3")));
        }

        // confidenceInterval: 基于historicalData计算
        // 格式化为 "均值±标准差"
        if (result.getHistoricalData() != null && !result.getHistoricalData().isEmpty()) {
            BigDecimal sum = BigDecimal.ZERO;
            int count = result.getHistoricalData().size();
            for (HistoricalData hd : result.getHistoricalData()) {
                if (hd.getConsumption() != null) {
                    sum = sum.add(hd.getConsumption());
                }
            }
            if (count > 0) {
                BigDecimal mean = sum.divide(new BigDecimal(count), 4, RoundingMode.HALF_UP);
                BigDecimal varianceSum = BigDecimal.ZERO;
                for (HistoricalData hd : result.getHistoricalData()) {
                    if (hd.getConsumption() != null) {
                        BigDecimal diff = hd.getConsumption().subtract(mean);
                        varianceSum = varianceSum.add(diff.multiply(diff));
                    }
                }
                BigDecimal stdDev = BigDecimal.ZERO;
                if (count > 1) {
                    stdDev = varianceSum.divide(new BigDecimal(count - 1), 4, RoundingMode.HALF_UP);
                    stdDev = new BigDecimal(Math.sqrt(stdDev.doubleValue()));
                }
                flatVO.setConfidenceInterval(mean.toPlainString() + "±" + stdDev.toPlainString());
            }
        }

        // recommendedPurchase = forecastTotal (前端自行计算与库存的差值)
        // 此处直接使用 forecastTotal
        if (flatVO.getForecastTotal() != null) {
            flatVO.setRecommendedPurchase(flatVO.getForecastTotal());
        }

        return flatVO;
    }

    /**
     * 补货点结果扁平化
     */
    public static ReorderPointFlatVO convertReorderPoint(ReorderPointResult result) {
        if (result == null) {
            return null;
        }

        ReorderPointFlatVO flatVO = new ReorderPointFlatVO();
        flatVO.setMaterialId(result.getMaterialId());
        flatVO.setMaterialName(result.getMaterialName());
        flatVO.setMaterialCode(result.getMaterialCode());
        flatVO.setCurrentStock(result.getCurrentStock());
        flatVO.setDailyConsumption(result.getDailyConsumption());
        flatVO.setLeadTime(result.getLeadTime());
        flatVO.setSafetyFactor(result.getSafetyFactor());
        flatVO.setSafetyStock(result.getSafetyStock());
        flatVO.setReorderPoint(result.getReorderPoint());
        flatVO.setNeedReorder(result.getNeedReorder());

        // 计算 stockStatus
        flatVO.setStockStatus(calculateStockStatus(result.getCurrentStock(), result.getSafetyStock()));

        return flatVO;
    }

    /**
     * 计算库存状态
     * currentStock < safetyStock → "LOW"
     * currentStock > safetyStock*2 → "HIGH"
     * 其他 → "NORMAL"
     */
    private static String calculateStockStatus(Integer currentStock, Integer safetyStock) {
        if (currentStock == null || safetyStock == null) {
            return "NORMAL";
        }
        if (currentStock < safetyStock) {
            return "LOW";
        }
        if (currentStock > safetyStock * 2) {
            return "HIGH";
        }
        return "NORMAL";
    }

    /**
     * 异常检测结果扁平化
     */
    public static AnomalyDetectionFlatVO convertAnomalyDetection(AnomalyDetectionResult result) {
        if (result == null) {
            return null;
        }

        AnomalyDetectionFlatVO flatVO = new AnomalyDetectionFlatVO();
        flatVO.setApplicationId(result.getApplicationId());
        flatVO.setApplicationNo(result.getApplicationNo());
        flatVO.setUserId(result.getUserId());
        flatVO.setUserName(result.getUserName());
        flatVO.setMaterialId(result.getMaterialId());
        flatVO.setMaterialName(result.getMaterialName());
        // materialCode not available in AnomalyDetectionResult, set to null
        flatVO.setMaterialCode(null);
        flatVO.setCurrentQuantity(result.getCurrentQuantity());
        flatVO.setHistoricalMean(result.getHistoricalMean());
        flatVO.setHistoricalStdDev(result.getHistoricalStdDev());
        flatVO.setThreshold(result.getThreshold());
        flatVO.setIsAnomaly(result.getIsAnomaly());
        flatVO.setDetectionTime(result.getDetectionTime());

        // anomalyType: isAnomaly ? "HIGH" : null
        if (Boolean.TRUE.equals(result.getIsAnomaly())) {
            flatVO.setAnomalyType("HIGH");
        } else {
            flatVO.setAnomalyType(null);
        }

        // detectedValue = currentQuantity
        if (result.getCurrentQuantity() != null) {
            flatVO.setDetectedValue(new BigDecimal(result.getCurrentQuantity()));
        }

        // normalRange = "均值-标准差 ~ 均值+标准差"
        if (result.getHistoricalMean() != null && result.getHistoricalStdDev() != null) {
            BigDecimal lower = result.getHistoricalMean().subtract(result.getHistoricalStdDev());
            BigDecimal upper = result.getHistoricalMean().add(result.getHistoricalStdDev());
            flatVO.setNormalRange(lower.toPlainString() + " ~ " + upper.toPlainString());
        }

        // deviation = ((currentQuantity - mean) / mean) * 100
        if (result.getCurrentQuantity() != null && result.getHistoricalMean() != null
                && result.getHistoricalMean().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal current = new BigDecimal(result.getCurrentQuantity());
            BigDecimal deviation = current.subtract(result.getHistoricalMean())
                    .divide(result.getHistoricalMean(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            flatVO.setDeviation(deviation);
        }

        // suggestedAction = "建议审批人仔细核实该申领"
        flatVO.setSuggestedAction("建议审批人仔细核实该申领");

        return flatVO;
    }
}
