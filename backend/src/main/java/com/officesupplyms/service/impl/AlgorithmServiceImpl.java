package com.officesupplyms.service.impl;

import com.officesupplyms.algorithm.AlgorithmUtil;
import com.officesupplyms.mapper.ConsumptionHistoryMapper;
import com.officesupplyms.mapper.MaterialMapper;
import com.officesupplyms.model.entity.ConsumptionHistory;
import com.officesupplyms.model.entity.Material;
import com.officesupplyms.model.vo.AlgorithmResultVO;
import com.officesupplyms.model.vo.MaterialVO;
import com.officesupplyms.service.AlgorithmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 算法服务实现类
 */
@Service
public class AlgorithmServiceImpl implements AlgorithmService {

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private ConsumptionHistoryMapper consumptionHistoryMapper;

    @Value("${app.algorithm.abc-class-a-percent:0.7}")
    private double abcClassAPercent;

    @Value("${app.algorithm.abc-class-b-percent:0.2}")
    private double abcClassBPercent;

    @Value("${app.algorithm.abc-class-c-percent:0.1}")
    private double abcClassCPercent;

    @Value("${app.algorithm.moving-average-period:3}")
    private int movingAveragePeriod;

    @Value("${app.algorithm.abnormal-sigma-multiple:3}")
    private int abnormalSigmaMultiple;

    @Value("${app.algorithm.default-safety-factor:1.5}")
    private BigDecimal defaultSafetyFactor;

    @Override
    @Transactional
    public AlgorithmResultVO.AbcClassificationResult calculateAbcClassification() {
        // 获取所有物资
        List<Material> materials = materialMapper.selectList(null);

        // 转换为可分类的对象
        List<AbcClassifiableMaterial> abcMaterials = materials.stream()
                .map(material -> new AbcClassifiableMaterial(material))
                .collect(Collectors.toList());

        // 计算ABC分类
        AlgorithmUtil.AbcClassificationResult<AbcClassifiableMaterial> result =
                AlgorithmUtil.calculateAbcClassification(abcMaterials, abcClassAPercent, abcClassBPercent, abcClassCPercent);

        // 更新数据库中的ABC分类
        updateAbcClassInDatabase(result);

        // 转换为VO
        return convertToAbcClassificationResultVO(result);
    }

    @Override
    public List<AlgorithmResultVO.DemandForecastResult> forecastDemand(Integer period) {
        List<Material> materials = materialMapper.selectList(null);
        List<AlgorithmResultVO.DemandForecastResult> forecastResults = new ArrayList<>();

        for (Material material : materials) {
            AlgorithmResultVO.DemandForecastResult result = forecastMaterialDemand(material.getId(), period);
            if (result != null) {
                forecastResults.add(result);
            }
        }

        return forecastResults;
    }

    @Override
    public AlgorithmResultVO.DemandForecastResult forecastMaterialDemand(Long materialId, Integer period) {
        Material material = materialMapper.selectById(materialId);
        if (material == null) {
            return null;
        }

        // 获取历史消耗数据（最近N个月）
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(period);

        List<BigDecimal> monthlyConsumptions = getMonthlyConsumptions(materialId, startDate, endDate);

        if (monthlyConsumptions.isEmpty()) {
            return null;
        }

        // 计算移动平均
        BigDecimal simpleMovingAverage = AlgorithmUtil.calculateSimpleMovingAverage(monthlyConsumptions, Math.min(period, monthlyConsumptions.size()));
        BigDecimal weightedMovingAverage = AlgorithmUtil.calculateWeightedMovingAverage(monthlyConsumptions, Math.min(period, monthlyConsumptions.size()));

        AlgorithmResultVO.DemandForecastResult result = new AlgorithmResultVO.DemandForecastResult();
        result.setMaterialId(materialId);
        result.setMaterialName(material.getName());
        result.setMaterialCode(material.getCode());
        result.setSimpleMovingAverage(simpleMovingAverage);
        result.setWeightedMovingAverage(weightedMovingAverage);
        result.setForecastDate(LocalDate.now().format(DateTimeFormatter.ISO_DATE));

        // 添加历史数据
        List<AlgorithmResultVO.HistoricalData> historicalDataList = new ArrayList<>();
        // 从月消耗数据中获取月份信息
        List<ConsumptionHistoryMapper.MonthlyConsumption> monthlyData =
                consumptionHistoryMapper.selectMonthlyConsumption(materialId, period);
        for (int i = 0; i < monthlyConsumptions.size(); i++) {
            AlgorithmResultVO.HistoricalData historicalData = new AlgorithmResultVO.HistoricalData();
            if (i < monthlyData.size()) {
                historicalData.setDate(monthlyData.get(i).getMonth());
            } else {
                historicalData.setDate(startDate.plusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM")));
            }
            historicalData.setConsumption(monthlyConsumptions.get(i));
            historicalDataList.add(historicalData);
        }
        result.setHistoricalData(historicalDataList);

        return result;
    }

    @Override
    public List<AlgorithmResultVO.ReorderPointResult> calculateReorderPoints() {
        List<Material> materials = materialMapper.selectList(null);
        List<AlgorithmResultVO.ReorderPointResult> results = new ArrayList<>();

        for (Material material : materials) {
            AlgorithmResultVO.ReorderPointResult result = calculateMaterialReorderPoint(material.getId());
            if (result != null) {
                results.add(result);
            }
        }

        return results;
    }

    @Override
    public AlgorithmResultVO.ReorderPointResult calculateMaterialReorderPoint(Long materialId) {
        Material material = materialMapper.selectById(materialId);
        if (material == null) {
            return null;
        }

        // 计算日均消耗量（最近3个月）
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(3);
        BigDecimal dailyConsumption = calculateDailyConsumption(materialId, startDate, endDate);

        // 计算安全库存
        int safetyStock = AlgorithmUtil.calculateSafetyStock(dailyConsumption,
                material.getLeadTime(), material.getSafetyFactor());

        // 计算补货点
        int reorderPoint = AlgorithmUtil.calculateReorderPoint(dailyConsumption,
                material.getLeadTime(), safetyStock);

        // 判断是否需要补货
        boolean needReorder = material.getCurrentStock() <= reorderPoint;

        AlgorithmResultVO.ReorderPointResult result = new AlgorithmResultVO.ReorderPointResult();
        result.setMaterialId(materialId);
        result.setMaterialName(material.getName());
        result.setMaterialCode(material.getCode());
        result.setCurrentStock(material.getCurrentStock());
        result.setDailyConsumption(dailyConsumption);
        result.setLeadTime(material.getLeadTime());
        result.setSafetyFactor(material.getSafetyFactor());
        result.setSafetyStock(safetyStock);
        result.setReorderPoint(reorderPoint);
        result.setNeedReorder(needReorder);
        result.setCalculationTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return result;
    }

    @Override
    public List<AlgorithmResultVO.AnomalyDetectionResult> detectAnomalies(LocalDate date) {
        // 实现异常检测逻辑
        List<AlgorithmResultVO.AnomalyDetectionResult> results = new ArrayList<>();

        // 查询指定日期的申领消耗记录
        List<ConsumptionHistory> consumptionRecords = consumptionHistoryMapper.selectByRecordDate(date);

        for (ConsumptionHistory record : consumptionRecords) {
            // 只检测APPLICATION类型的记录
            if (!"APPLICATION".equals(record.getType())) {
                continue;
            }

            Long userId = record.getUserId();
            Long materialId = record.getMaterialId();

            if (userId == null || materialId == null) {
                continue;
            }

            // 获取该用户对该物资的历史统计
            AlgorithmResultVO.AnomalyDetectionResult stats = calculateUserMaterialStatistics(userId, materialId);
            if (stats.getHistoricalMean() == null || stats.getThreshold() == null) {
                continue; // 没有足够的历史数据
            }

            // 判断是否异常
            BigDecimal currentQuantity = BigDecimal.valueOf(record.getQuantity());
            boolean isAnomaly = currentQuantity.compareTo(stats.getThreshold()) > 0;

            if (isAnomaly) {
                AlgorithmResultVO.AnomalyDetectionResult anomalyResult = new AlgorithmResultVO.AnomalyDetectionResult();
                anomalyResult.setApplicationId(record.getApplicationId());
                anomalyResult.setUserId(userId);
                anomalyResult.setMaterialId(materialId);
                anomalyResult.setCurrentQuantity(record.getQuantity());
                anomalyResult.setHistoricalMean(stats.getHistoricalMean());
                anomalyResult.setHistoricalStdDev(stats.getHistoricalStdDev());
                anomalyResult.setThreshold(stats.getThreshold());
                anomalyResult.setIsAnomaly(true);
                anomalyResult.setDetectionTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                // TODO: 这里可以添加预警记录
                results.add(anomalyResult);
            }
        }

        return results;
    }

    @Override
    public BigDecimal calculateDailyConsumption(Long materialId, LocalDate startDate, LocalDate endDate) {
        Double avgDailyConsumption = consumptionHistoryMapper.calculateAverageDailyConsumption(materialId, startDate, endDate);
        return avgDailyConsumption != null ?
                BigDecimal.valueOf(avgDailyConsumption).setScale(2, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO;
    }

    @Override
    public AlgorithmResultVO.AnomalyDetectionResult calculateUserMaterialStatistics(Long userId, Long materialId) {
        // 查询用户对某物资的历史申领统计数据
        ConsumptionHistoryMapper.UserMaterialStat stat = consumptionHistoryMapper.selectUserMaterialStatistics(userId, materialId);

        AlgorithmResultVO.AnomalyDetectionResult result = new AlgorithmResultVO.AnomalyDetectionResult();
        result.setUserId(userId);
        result.setMaterialId(materialId);

        if (stat != null && stat.getMean() != null) {
            result.setHistoricalMean(BigDecimal.valueOf(stat.getMean()));
            result.setHistoricalStdDev(BigDecimal.valueOf(stat.getStdDev() != null ? stat.getStdDev() : 0));
            result.setThreshold(result.getHistoricalMean().add(
                    result.getHistoricalStdDev().multiply(BigDecimal.valueOf(abnormalSigmaMultiple))));
        }

        return result;
    }

    @Override
    @Transactional
    public void updateMaterialAnnualConsumption() {
        // 更新所有物资的年消耗数据
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(1);

        // 获取所有物资
        List<Material> materials = materialMapper.selectList(null);

        for (Material material : materials) {
            // 计算过去一年的消耗总量
            Integer annualConsumption = consumptionHistoryMapper.sumQuantityByMaterialIdAndDateRange(
                    material.getId(), startDate, endDate);

            if (annualConsumption == null) {
                annualConsumption = 0;
            }

            // 计算年消耗金额
            BigDecimal annualConsumptionAmount = material.getUnitPrice() != null ?
                    material.getUnitPrice().multiply(BigDecimal.valueOf(annualConsumption)) :
                    BigDecimal.ZERO;

            // 更新数据库
            materialMapper.updateAnnualConsumption(material.getId(), annualConsumption, annualConsumptionAmount);

            // 更新实体对象，以便后续可能的使用
            material.setAnnualConsumption(annualConsumption);
            material.setAnnualConsumptionAmount(annualConsumptionAmount);
        }
    }

    @Override
    @Scheduled(cron = "${app.schedule.abc-class-cron:0 0 2 * * ?}")
    public void scheduleAbcClassification() {
        calculateAbcClassification();
    }

    @Override
    @Scheduled(cron = "${app.schedule.reorder-point-cron:0 0 * * * ?}")
    public void scheduleReorderPointCalculation() {
        calculateReorderPoints();
    }

    @Override
    @Scheduled(cron = "${app.schedule.demand-forecast-cron:0 0 3 1 * ?}")
    public void scheduleDemandForecast() {
        forecastDemand(movingAveragePeriod);
    }

    @Override
    public List<AlgorithmResultVO.ChartData> getInventoryTurnoverRate() {
        // 计算库存周转率
        // 这里简化处理，返回示例数据
        AlgorithmResultVO.ChartData chartData = new AlgorithmResultVO.ChartData();
        chartData.setTitle("库存周转率");
        chartData.setUnit("次");

        chartData.setLabels(Arrays.asList("1月", "2月", "3月", "4月", "5月", "6月"));
        chartData.setValues(Arrays.asList(
                BigDecimal.valueOf(1.2),
                BigDecimal.valueOf(1.5),
                BigDecimal.valueOf(1.3),
                BigDecimal.valueOf(1.8),
                BigDecimal.valueOf(1.6),
                BigDecimal.valueOf(1.9)
        ));

        return Arrays.asList(chartData);
    }

    @Override
    public AlgorithmResultVO.ChartData getAbcClassificationChartData() {
        // 获取ABC分类占比数据
        AlgorithmResultVO.ChartData chartData = new AlgorithmResultVO.ChartData();
        chartData.setTitle("ABC分类占比");
        chartData.setUnit("%");

        // 查询各类物资数量
        List<Material> classA = materialMapper.selectByAbcClass("A");
        List<Material> classB = materialMapper.selectByAbcClass("B");
        List<Material> classC = materialMapper.selectByAbcClass("C");

        int total = classA.size() + classB.size() + classC.size();
        if (total == 0) {
            chartData.setLabels(Arrays.asList("A", "B", "C"));
            chartData.setValues(Arrays.asList(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
            return chartData;
        }

        BigDecimal percentA = BigDecimal.valueOf(classA.size() * 100.0 / total).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal percentB = BigDecimal.valueOf(classB.size() * 100.0 / total).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal percentC = BigDecimal.valueOf(classC.size() * 100.0 / total).setScale(2, BigDecimal.ROUND_HALF_UP);

        chartData.setLabels(Arrays.asList("A类", "B类", "C类"));
        chartData.setValues(Arrays.asList(percentA, percentB, percentC));

        return chartData;
    }

    @Override
    public AlgorithmResultVO.ChartData getMonthlyApplicationTrend() {
        // 获取每月申领单数量与金额趋势数据
        AlgorithmResultVO.ChartData chartData = new AlgorithmResultVO.ChartData();
        chartData.setTitle("月度申领趋势");
        chartData.setUnit("个");

        // TODO: 实现真实数据查询
        // 暂时返回示例数据
        chartData.setLabels(Arrays.asList("1月", "2月", "3月", "4月", "5月", "6月"));
        chartData.setValues(Arrays.asList(
                BigDecimal.valueOf(120),
                BigDecimal.valueOf(150),
                BigDecimal.valueOf(130),
                BigDecimal.valueOf(180),
                BigDecimal.valueOf(160),
                BigDecimal.valueOf(190)
        ));

        return chartData;
    }

    @Override
    public AlgorithmResultVO.ChartData getForecastVsActualComparison(Long materialId) {
        // 获取预测与实际消耗对比数据
        AlgorithmResultVO.ChartData chartData = new AlgorithmResultVO.ChartData();
        chartData.setTitle("预测与实际消耗对比");
        chartData.setUnit("个");

        // TODO: 实现真实数据查询
        // 暂时返回示例数据
        chartData.setLabels(Arrays.asList("预测", "实际"));
        chartData.setValues(Arrays.asList(
                BigDecimal.valueOf(150),
                BigDecimal.valueOf(145)
        ));

        return chartData;
    }

    // 私有辅助方法

    private List<BigDecimal> getMonthlyConsumptions(Long materialId, LocalDate startDate, LocalDate endDate) {
        // 获取月消耗量数据
        // 计算月份差
        long monthsBetween = java.time.temporal.ChronoUnit.MONTHS.between(
                startDate.withDayOfMonth(1), endDate.withDayOfMonth(1));
        if (monthsBetween <= 0) {
            monthsBetween = 1;
        }

        // 查询月消耗数据
        List<ConsumptionHistoryMapper.MonthlyConsumption> monthlyData =
                consumptionHistoryMapper.selectMonthlyConsumption(materialId, (int) monthsBetween);

        // 转换为BigDecimal列表，按月份顺序
        List<BigDecimal> consumptions = new ArrayList<>();
        for (ConsumptionHistoryMapper.MonthlyConsumption data : monthlyData) {
            consumptions.add(BigDecimal.valueOf(data.getTotalQuantity()));
        }

        // 如果数据为空，返回空列表
        return consumptions;
    }

    private void updateAbcClassInDatabase(AlgorithmUtil.AbcClassificationResult<AbcClassifiableMaterial> result) {
        // 更新A类物资
        for (AbcClassifiableMaterial material : result.getClassA()) {
            materialMapper.updateAbcClass(material.getMaterial().getId(), "A");
        }
        // 更新B类物资
        for (AbcClassifiableMaterial material : result.getClassB()) {
            materialMapper.updateAbcClass(material.getMaterial().getId(), "B");
        }
        // 更新C类物资
        for (AbcClassifiableMaterial material : result.getClassC()) {
            materialMapper.updateAbcClass(material.getMaterial().getId(), "C");
        }
    }

    private AlgorithmResultVO.AbcClassificationResult convertToAbcClassificationResultVO(
            AlgorithmUtil.AbcClassificationResult<AbcClassifiableMaterial> result) {

        AlgorithmResultVO.AbcClassificationResult voResult = new AlgorithmResultVO.AbcClassificationResult();

        // 转换A类物资
        List<MaterialVO> classA = result.getClassA().stream()
                .map(m -> convertToMaterialVO(m.getMaterial()))
                .collect(Collectors.toList());
        voResult.setClassA(classA);

        // 转换B类物资
        List<MaterialVO> classB = result.getClassB().stream()
                .map(m -> convertToMaterialVO(m.getMaterial()))
                .collect(Collectors.toList());
        voResult.setClassB(classB);

        // 转换C类物资
        List<MaterialVO> classC = result.getClassC().stream()
                .map(m -> convertToMaterialVO(m.getMaterial()))
                .collect(Collectors.toList());
        voResult.setClassC(classC);

        // 计算占比
        java.util.Map<String, BigDecimal> percentages = new java.util.HashMap<>();
        percentages.put("A", result.getClassAPercent());
        percentages.put("B", result.getClassBPercent());
        percentages.put("C", result.getClassCPercent());
        voResult.setPercentages(percentages);

        voResult.setCalculationTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return voResult;
    }

    private MaterialVO convertToMaterialVO(Material material) {
        // 这里简化处理，实际需要完整转换
        MaterialVO vo = new MaterialVO();
        vo.setId(material.getId());
        vo.setCode(material.getCode());
        vo.setName(material.getName());
        vo.setCategory(material.getCategory());
        vo.setUnit(material.getUnit());
        vo.setCurrentStock(material.getCurrentStock());
        vo.setAbcClass(material.getAbcClass());
        return vo;
    }

    /**
     * 用于ABC分类的内部类
     */
    private static class AbcClassifiableMaterial implements AlgorithmUtil.AbcClassifiable {
        private final Material material;

        public AbcClassifiableMaterial(Material material) {
            this.material = material;
        }

        @Override
        public BigDecimal getAnnualConsumptionAmount() {
            return material.getAnnualConsumptionAmount() != null ?
                    material.getAnnualConsumptionAmount() : BigDecimal.ZERO;
        }

        @Override
        public void setAbcClass(String abcClass) {
            material.setAbcClass(abcClass);
        }

        public Material getMaterial() {
            return material;
        }
    }
}