package com.officesupplyms.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 算法工具类
 */
public class AlgorithmUtil {

    /**
     * 计算简单移动平均
     *
     * @param data 历史数据列表
     * @param period 移动平均周期
     * @return 移动平均值
     */
    public static BigDecimal calculateSimpleMovingAverage(List<BigDecimal> data, int period) {
        if (data == null || data.size() < period) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (int i = data.size() - period; i < data.size(); i++) {
            sum = sum.add(data.get(i));
        }

        return sum.divide(BigDecimal.valueOf(period), 2, RoundingMode.HALF_UP);
    }

    /**
     * 计算加权移动平均
     *
     * @param data 历史数据列表
     * @param period 移动平均周期
     * @return 加权移动平均值
     */
    public static BigDecimal calculateWeightedMovingAverage(List<BigDecimal> data, int period) {
        if (data == null || data.size() < period) {
            return BigDecimal.ZERO;
        }

        BigDecimal weightedSum = BigDecimal.ZERO;
        int weightSum = 0;

        for (int i = 0; i < period; i++) {
            int weight = i + 1; // 近期权重高
            weightedSum = weightedSum.add(data.get(data.size() - period + i).multiply(BigDecimal.valueOf(weight)));
            weightSum += weight;
        }

        return weightedSum.divide(BigDecimal.valueOf(weightSum), 2, RoundingMode.HALF_UP);
    }

    /**
     * 计算均值
     */
    public static BigDecimal calculateMean(List<BigDecimal> data) {
        if (data == null || data.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal value : data) {
            sum = sum.add(value);
        }

        return sum.divide(BigDecimal.valueOf(data.size()), 4, RoundingMode.HALF_UP);
    }

    /**
     * 计算标准差
     */
    public static BigDecimal calculateStandardDeviation(List<BigDecimal> data) {
        if (data == null || data.size() < 2) {
            return BigDecimal.ZERO;
        }

        BigDecimal mean = calculateMean(data);
        BigDecimal sumSquaredDiff = BigDecimal.ZERO;

        for (BigDecimal value : data) {
            BigDecimal diff = value.subtract(mean);
            sumSquaredDiff = sumSquaredDiff.add(diff.multiply(diff));
        }

        BigDecimal variance = sumSquaredDiff.divide(BigDecimal.valueOf(data.size() - 1), 8, RoundingMode.HALF_UP);
        return sqrt(variance, 4);
    }

    /**
     * 开平方根（简化实现）
     */
    private static BigDecimal sqrt(BigDecimal value, int scale) {
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal x0 = BigDecimal.ZERO;
        BigDecimal x1 = BigDecimal.valueOf(Math.sqrt(value.doubleValue()));

        while (!x0.equals(x1)) {
            x0 = x1;
            x1 = value.divide(x0, scale * 2, RoundingMode.HALF_UP);
            x1 = x1.add(x0);
            x1 = x1.divide(BigDecimal.valueOf(2), scale * 2, RoundingMode.HALF_UP);
        }

        return x1.setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * 判断是否为异常值（3σ原则）
     *
     * @param value 当前值
     * @param mean 均值
     * @param stdDev 标准差
     * @param sigmaMultiple 标准差倍数（默认为3）
     * @return 是否异常
     */
    public static boolean isAnomaly(BigDecimal value, BigDecimal mean, BigDecimal stdDev, int sigmaMultiple) {
        BigDecimal threshold = mean.add(stdDev.multiply(BigDecimal.valueOf(sigmaMultiple)));
        return value.compareTo(threshold) > 0;
    }

    /**
     * 计算安全库存
     * 安全库存 = 日均消耗量 × 采购提前期 × 安全系数
     *
     * @param dailyConsumption 日均消耗量
     * @param leadTime 采购提前期（天）
     * @param safetyFactor 安全系数
     * @return 安全库存
     */
    public static int calculateSafetyStock(BigDecimal dailyConsumption, int leadTime, BigDecimal safetyFactor) {
        BigDecimal safetyStock = dailyConsumption
                .multiply(BigDecimal.valueOf(leadTime))
                .multiply(safetyFactor);
        return safetyStock.setScale(0, RoundingMode.UP).intValue();
    }

    /**
     * 计算补货点
     * 补货点 = 日均消耗量 × 采购提前期 + 安全库存
     *
     * @param dailyConsumption 日均消耗量
     * @param leadTime 采购提前期（天）
     * @param safetyStock 安全库存
     * @return 补货点
     */
    public static int calculateReorderPoint(BigDecimal dailyConsumption, int leadTime, int safetyStock) {
        BigDecimal reorderPoint = dailyConsumption
                .multiply(BigDecimal.valueOf(leadTime))
                .add(BigDecimal.valueOf(safetyStock));
        return reorderPoint.setScale(0, RoundingMode.UP).intValue();
    }

    /**
     * ABC分类计算
     *
     * @param materials 物资列表（包含年消耗金额）
     * @param aPercent A类占比阈值（如0.7）
     * @param bPercent B类占比阈值（如0.2）
     * @param cPercent C类占比阈值（如0.1）
     * @return 分类结果
     */
    public static <T extends AbcClassifiable> AbcClassificationResult<T> calculateAbcClassification(
            List<T> materials, double aPercent, double bPercent, double cPercent) {

        // 按年消耗金额降序排序
        materials.sort((m1, m2) -> m2.getAnnualConsumptionAmount().compareTo(m1.getAnnualConsumptionAmount()));

        // 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (T material : materials) {
            totalAmount = totalAmount.add(material.getAnnualConsumptionAmount());
        }

        // 分类
        List<T> classA = new ArrayList<>();
        List<T> classB = new ArrayList<>();
        List<T> classC = new ArrayList<>();

        BigDecimal cumulativeAmount = BigDecimal.ZERO;

        for (T material : materials) {
            cumulativeAmount = cumulativeAmount.add(material.getAnnualConsumptionAmount());
            BigDecimal cumulativePercent = cumulativeAmount.divide(totalAmount, 4, RoundingMode.HALF_UP);

            if (cumulativePercent.doubleValue() <= aPercent) {
                classA.add(material);
            } else if (cumulativePercent.doubleValue() <= aPercent + bPercent) {
                classB.add(material);
            } else {
                classC.add(material);
            }
        }

        return new AbcClassificationResult<>(classA, classB, classC, totalAmount);
    }

    /**
     * ABC分类可接口
     */
    public interface AbcClassifiable {
        BigDecimal getAnnualConsumptionAmount();
        void setAbcClass(String abcClass);
    }

    /**
     * ABC分类结果
     */
    public static class AbcClassificationResult<T extends AbcClassifiable> {
        private final List<T> classA;
        private final List<T> classB;
        private final List<T> classC;
        private final BigDecimal totalAmount;

        public AbcClassificationResult(List<T> classA, List<T> classB, List<T> classC, BigDecimal totalAmount) {
            this.classA = classA;
            this.classB = classB;
            this.classC = classC;
            this.totalAmount = totalAmount;
        }

        public List<T> getClassA() { return classA; }
        public List<T> getClassB() { return classB; }
        public List<T> getClassC() { return classC; }
        public BigDecimal getTotalAmount() { return totalAmount; }

        public BigDecimal getClassAPercent() {
            return calculateClassPercent(classA);
        }

        public BigDecimal getClassBPercent() {
            return calculateClassPercent(classB);
        }

        public BigDecimal getClassCPercent() {
            return calculateClassPercent(classC);
        }

        private BigDecimal calculateClassPercent(List<T> materials) {
            BigDecimal classAmount = BigDecimal.ZERO;
            for (T material : materials) {
                classAmount = classAmount.add(material.getAnnualConsumptionAmount());
            }
            return classAmount.divide(totalAmount, 4, RoundingMode.HALF_UP);
        }
    }
}