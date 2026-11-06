package com.officesupplyms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.officesupplyms.algorithm.AlgorithmUtil;
import com.officesupplyms.converter.AlgorithmResultConverter;
import com.officesupplyms.mapper.ApplicationMapper;
import com.officesupplyms.mapper.ConsumptionHistoryMapper;
import com.officesupplyms.mapper.MaterialMapper;
import com.officesupplyms.mapper.StockInMapper;
import com.officesupplyms.mapper.StockOutMapper;
import com.officesupplyms.mapper.UserMapper;
import com.officesupplyms.model.entity.Application;
import com.officesupplyms.model.entity.ConsumptionHistory;
import com.officesupplyms.model.entity.Material;
import com.officesupplyms.model.entity.StockIn;
import com.officesupplyms.model.entity.StockOut;
import com.officesupplyms.model.entity.User;
import com.officesupplyms.model.vo.AlgorithmResultVO;
import com.officesupplyms.model.vo.MaterialVO;
import com.officesupplyms.service.AlgorithmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private ApplicationMapper applicationMapper;

    @Autowired
    private ConsumptionHistoryMapper consumptionHistoryMapper;

    @Autowired
    private StockInMapper stockInMapper;

    @Autowired
    private StockOutMapper stockOutMapper;

    @Autowired
    private UserMapper userMapper;

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

        // 获取历史消耗数据（扩大时间范围到5年，确保包含所有历史数据）
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(5);

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
        // 使用日期范围获取月消耗数据
        List<ConsumptionHistoryMapper.MonthlyConsumption> monthlyData =
                consumptionHistoryMapper.selectMonthlyConsumptionByDateRange(materialId, startDate, endDate);
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

        // 计算日均消耗量（使用5年历史数据，与ABC分类保持一致）
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(5);
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
        List<AlgorithmResultVO.AnomalyDetectionResult> results = new ArrayList<>();

        // 查询指定日期的申领记录
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        List<Application> applications = applicationMapper.selectByTimeRange(startOfDay, endOfDay);

        // 同时查询所有已完成的申领单作为历史数据
        List<Application> allCompleted = applicationMapper.selectByStatus("COMPLETED");

        for (Application app : applications) {
            Long userId = app.getUserId();
            Long materialId = app.getMaterialId();

            if (userId == null || materialId == null) {
                continue;
            }

            // 获取该用户对该物资的历史统计（从已完成的申领单中计算）
            AlgorithmResultVO.AnomalyDetectionResult stats = calculateApplicationStatistics(userId, materialId, app.getId());
            if (stats.getHistoricalMean() == null || stats.getThreshold() == null) {
                continue; // 没有足够的历史数据
            }

            // 判断是否异常：当前申领数量 > 均值 + 3*标准差
            BigDecimal currentQuantity = BigDecimal.valueOf(app.getQuantity());
            boolean isAnomaly = currentQuantity.compareTo(stats.getThreshold()) > 0;

            if (isAnomaly) {
                AlgorithmResultVO.AnomalyDetectionResult anomalyResult = new AlgorithmResultVO.AnomalyDetectionResult();
                anomalyResult.setApplicationId(app.getId());
                anomalyResult.setUserId(userId);
                anomalyResult.setMaterialId(materialId);
                anomalyResult.setCurrentQuantity(app.getQuantity());
                anomalyResult.setHistoricalMean(stats.getHistoricalMean());
                anomalyResult.setHistoricalStdDev(stats.getHistoricalStdDev());
                anomalyResult.setThreshold(stats.getThreshold());
                anomalyResult.setIsAnomaly(true);
                anomalyResult.setDetectionTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                results.add(anomalyResult);
            }
        }

        return results;
    }

    /**
     * 计算用户对某物资的申领统计（用于异常检测）
     */
    private AlgorithmResultVO.AnomalyDetectionResult calculateApplicationStatistics(Long userId, Long materialId, Long excludeAppId) {
        List<Application> history = applicationMapper.selectUserMaterialHistory(userId, materialId);

        AlgorithmResultVO.AnomalyDetectionResult result = new AlgorithmResultVO.AnomalyDetectionResult();
        result.setUserId(userId);
        result.setMaterialId(materialId);

        if (history == null || history.size() < 2) {
            return result; // 没有足够的历史数据
        }

        // 排除当前申请
        List<Integer> quantities = history.stream()
                .filter(app -> !app.getId().equals(excludeAppId))
                .map(Application::getQuantity)
                .collect(Collectors.toList());

        if (quantities.size() < 2) {
            return result;
        }

        // 计算均值和标准差
        double mean = quantities.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double variance = quantities.stream()
                .mapToDouble(q -> Math.pow(q - mean, 2))
                .average().orElse(0.0);
        double stdDev = Math.sqrt(variance);

        // 阈值 = 均值 + 3*标准差
        BigDecimal threshold = BigDecimal.valueOf(mean + 3 * stdDev);

        result.setHistoricalMean(BigDecimal.valueOf(mean));
        result.setHistoricalStdDev(BigDecimal.valueOf(stdDev));
        result.setThreshold(threshold);

        return result;
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
    public AlgorithmResultVO.AnomalyDetectionResult calculateMaterialStatistics(Long materialId) {
        // 查询某物资的历史申领统计数据（按物资维度，不区分用户）
        ConsumptionHistoryMapper.UserMaterialStat stat = consumptionHistoryMapper.selectMaterialStatistics(materialId);

        AlgorithmResultVO.AnomalyDetectionResult result = new AlgorithmResultVO.AnomalyDetectionResult();
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
        // 更新所有物资的年消耗数据（使用所有历史数据）
        LocalDate endDate = LocalDate.now();
        // 扩大时间范围到5年前，确保历史测试数据都能参与计算
        LocalDate startDate = endDate.minusYears(5);

        // 获取所有物资
        List<Material> materials = materialMapper.selectList(null);

        for (Material material : materials) {
            // 计算过去5年内的消耗总量（确保包含所有历史数据）
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
        // 计算库存周转率 = 出库数量 / 平均库存
        AlgorithmResultVO.ChartData chartData = new AlgorithmResultVO.ChartData();
        chartData.setTitle("库存周转率");
        chartData.setUnit("次");

        // 获取最近6个月的数据
        LocalDate now = LocalDate.now();
        List<String> labels = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1);
            String monthLabel = monthStart.format(DateTimeFormatter.ofPattern("MM月"));
            labels.add(monthLabel);

            // 计算该月的出库总量
            List<StockOut> stockOuts = stockOutMapper.selectByTimeRange(
                    monthStart.atStartOfDay(),
                    monthEnd.atStartOfDay()
            );
            int totalOut = stockOuts.stream().mapToInt(StockOut::getQuantity).sum();

            // 计算平均库存（简化处理：使用期间入库总量作为代理）
            List<StockIn> stockIns = stockInMapper.selectByTimeRange(
                    monthStart.atStartOfDay(),
                    monthEnd.atStartOfDay()
            );
            int totalIn = stockIns.stream().mapToInt(StockIn::getQuantity).sum();

            // 平均库存 = (期初库存 + 期末库存) / 2，简化用期间入库量
            double avgStock = (totalIn + totalOut) / 2.0;
            double turnoverRate = avgStock > 0 ? totalOut / avgStock : 0;
            values.add(BigDecimal.valueOf(turnoverRate).setScale(2, RoundingMode.HALF_UP));
        }

        chartData.setLabels(labels);
        chartData.setValues(values);

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
            chartData.setCounts(Arrays.asList(0, 0, 0));
            return chartData;
        }

        BigDecimal percentA = BigDecimal.valueOf(classA.size() * 100.0 / total).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal percentB = BigDecimal.valueOf(classB.size() * 100.0 / total).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal percentC = BigDecimal.valueOf(classC.size() * 100.0 / total).setScale(2, BigDecimal.ROUND_HALF_UP);

        chartData.setLabels(Arrays.asList("A类", "B类", "C类"));
        chartData.setValues(Arrays.asList(percentA, percentB, percentC));
        chartData.setCounts(Arrays.asList(classA.size(), classB.size(), classC.size()));

        return chartData;
    }

    @Override
    public AlgorithmResultVO.ChartData getMonthlyApplicationTrend() {
        // 获取每月申领单数量与金额趋势数据
        AlgorithmResultVO.ChartData chartData = new AlgorithmResultVO.ChartData();
        chartData.setTitle("月度申领趋势");
        chartData.setUnit("个");

        // 查询每月申领统计数据
        List<ApplicationMapper.MonthlyApplicationStat> monthlyStats = applicationMapper.selectMonthlyApplicationStats();

        if (monthlyStats != null && !monthlyStats.isEmpty()) {
            List<String> labels = monthlyStats.stream()
                    .map(ApplicationMapper.MonthlyApplicationStat::getMonth)
                    .collect(Collectors.toList());
            List<BigDecimal> values = monthlyStats.stream()
                    .map(stat -> BigDecimal.valueOf(stat.getApplicationCount()))
                    .collect(Collectors.toList());
            chartData.setLabels(labels);
            chartData.setValues(values);
        } else {
            // 如果没有数据，返回空数据
            chartData.setLabels(new ArrayList<>());
            chartData.setValues(new ArrayList<>());
        }

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

    @Override
    public List<AlgorithmResultVO.AnomalyDetectionFlatVO> getAnomalousApplications() {
        List<AlgorithmResultVO.AnomalyDetectionFlatVO> results = new ArrayList<>();

        // 查询所有被标记为异常的申领记录
        List<Application> abnormalApps = applicationMapper.selectByStatusAndIsAbnormal();

        for (Application app : abnormalApps) {
            // 查询该物资的所有历史申领记录（按物资维度，排除当前申请）
            List<Application> allHistory = applicationMapper.selectAllMaterialHistory(app.getMaterialId());

            // 排除当前申请
            List<Integer> quantities = allHistory.stream()
                    .filter(a -> !a.getId().equals(app.getId()))
                    .map(Application::getQuantity)
                    .collect(Collectors.toList());

            AlgorithmResultVO.AnomalyDetectionFlatVO vo = new AlgorithmResultVO.AnomalyDetectionFlatVO();
            vo.setApplicationId(app.getId());
            vo.setApplicationNo(app.getApplicationNo());
            vo.setUserId(app.getUserId());
            vo.setMaterialId(app.getMaterialId());
            vo.setCurrentQuantity(app.getQuantity());
            vo.setIsAnomaly(true);
            vo.setAbnormalReason(app.getAbnormalReason());

            // 计算统计数据（按物资维度）
            if (quantities.size() >= 2) {
                double mean = quantities.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                double variance = quantities.stream()
                        .mapToDouble(q -> Math.pow(q - mean, 2))
                        .average().orElse(0.0);
                double stdDev = Math.sqrt(variance);

                BigDecimal historicalMean = BigDecimal.valueOf(mean);
                BigDecimal historicalStdDev = BigDecimal.valueOf(stdDev);
                BigDecimal threshold = historicalMean.add(historicalStdDev.multiply(BigDecimal.valueOf(abnormalSigmaMultiple)));

                vo.setHistoricalMean(historicalMean);
                vo.setHistoricalStdDev(historicalStdDev);
                vo.setThreshold(threshold);

                // 计算偏离程度
                if (mean > 0) {
                    BigDecimal deviation = BigDecimal.valueOf(app.getQuantity())
                            .subtract(historicalMean)
                            .divide(historicalMean, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    vo.setDeviation(deviation.setScale(2, RoundingMode.HALF_UP));
                }
            } else if (quantities.size() == 1) {
                // 只有1条历史记录，只能计算均值
                double mean = quantities.get(0);
                BigDecimal historicalMean = BigDecimal.valueOf(mean);
                vo.setHistoricalMean(historicalMean);
                vo.setHistoricalStdDev(BigDecimal.ZERO);
                // 无法计算标准差，设置阈值为null
                vo.setThreshold(null);
            }

            // 设置物资和用户信息
            Material material = materialMapper.selectById(app.getMaterialId());
            if (material != null) {
                vo.setMaterialName(material.getName());
                vo.setMaterialCode(material.getCode());
            }
            User user = userMapper.selectById(app.getUserId());
            if (user != null) {
                vo.setUserName(user.getRealName());
                vo.setUserDepartment(user.getDepartment());
            }

            vo.setApplyTime(app.getApplyTime() != null ?
                    app.getApplyTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
            vo.setDetectionTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            results.add(vo);
        }

        return results;
    }

    @Override
    public AlgorithmResultVO.ChartData getDepartmentApplicationStats() {
        // 获取部门申领统计数据
        AlgorithmResultVO.ChartData chartData = new AlgorithmResultVO.ChartData();
        chartData.setTitle("部门申领统计");
        chartData.setUnit("个");

        // 查询部门申领统计
        List<ApplicationMapper.DepartmentApplicationStat> stats = applicationMapper.selectDepartmentApplicationStats();

        if (stats != null && !stats.isEmpty()) {
            List<String> labels = stats.stream()
                    .map(ApplicationMapper.DepartmentApplicationStat::getDepartment)
                    .collect(Collectors.toList());
            List<BigDecimal> values = stats.stream()
                    .map(stat -> stat.getTotalQuantity() != null ?
                            stat.getTotalQuantity() : BigDecimal.ZERO)
                    .collect(Collectors.toList());
            chartData.setLabels(labels);
            chartData.setValues(values);
        } else {
            chartData.setLabels(new ArrayList<>());
            chartData.setValues(new ArrayList<>());
        }

        return chartData;
    }

    @Override
    public AlgorithmResultVO.ChartData getStockStatusDistribution() {
        // 获取库存状态分布数据
        AlgorithmResultVO.ChartData chartData = new AlgorithmResultVO.ChartData();
        chartData.setTitle("库存状态分布");
        chartData.setUnit("个");

        // 获取所有启用的物资
        QueryWrapper<Material> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        List<Material> materials = materialMapper.selectList(queryWrapper);

        // 使用与补货点计算相同的逻辑判断库存状态
        int normalCount = 0;
        int lowCount = 0;
        int highCount = 0;

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(5);

        for (Material material : materials) {
            // 计算日均消耗（与补货点计算一致，使用5年数据）
            BigDecimal dailyConsumption = calculateDailyConsumption(material.getId(), startDate, endDate);

            // 计算安全库存
            int safetyStock = AlgorithmUtil.calculateSafetyStock(dailyConsumption,
                    material.getLeadTime(), material.getSafetyFactor());

            // 判断库存状态（与AlgorithmResultConverter.calculateStockStatus逻辑一致）
            String stockStatus = calculateStockStatusForMaterial(material.getCurrentStock(), safetyStock);
            switch (stockStatus) {
                case "LOW":
                    lowCount++;
                    break;
                case "HIGH":
                    highCount++;
                    break;
                default:
                    normalCount++;
                    break;
            }
        }

        List<String> labels = Arrays.asList("正常", "库存过低", "库存过高");
        List<BigDecimal> values = Arrays.asList(
                BigDecimal.valueOf(normalCount),
                BigDecimal.valueOf(lowCount),
                BigDecimal.valueOf(highCount)
        );
        chartData.setLabels(labels);
        chartData.setValues(values);

        return chartData;
    }

    /**
     * 计算库存状态（与AlgorithmResultConverter.calculateStockStatus逻辑一致）
     */
    private String calculateStockStatusForMaterial(Integer currentStock, Integer safetyStock) {
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

    // 私有辅助方法

    private List<BigDecimal> getMonthlyConsumptions(Long materialId, LocalDate startDate, LocalDate endDate) {
        // 使用日期范围查询月消耗数据
        List<ConsumptionHistoryMapper.MonthlyConsumption> monthlyData =
                consumptionHistoryMapper.selectMonthlyConsumptionByDateRange(materialId, startDate, endDate);

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
        MaterialVO vo = new MaterialVO();
        vo.setId(material.getId());
        vo.setCode(material.getCode());
        vo.setName(material.getName());
        vo.setCategory(material.getCategory());
        vo.setUnit(material.getUnit());
        vo.setCurrentStock(material.getCurrentStock());
        vo.setAbcClass(material.getAbcClass());
        vo.setAnnualConsumptionAmount(material.getAnnualConsumptionAmount());
        return vo;
    }

    /**
     * 用于ABC分类的内部类
     */
    private static class AbcClassifiableMaterial implements AlgorithmUtil.AbcClassifiable {
        private final Material material;
        private BigDecimal calculatedAmount; // 实时计算的消耗金额

        public AbcClassifiableMaterial(Material material) {
            this.material = material;
        }

        public void setCalculatedAmount(BigDecimal amount) {
            this.calculatedAmount = amount;
        }

        @Override
        public BigDecimal getAnnualConsumptionAmount() {
            // 优先返回计算出的金额，否则返回数据库中的值
            if (calculatedAmount != null) {
                return calculatedAmount;
            }
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

    @Override
    public List<AlgorithmResultVO.AbcClassificationFlatVO> calculateAbcClassificationFlat() {
        // 实时从消耗历史计算ABC分类
        List<Material> materials = materialMapper.selectList(null);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(5); // 扩大范围到5年

        // 用于ABC计算的对象列表
        List<AbcClassifiableMaterial> abcMaterials = new ArrayList<>();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Material material : materials) {
            // 实时查询该物资的历史消耗总量
            Integer totalConsumption = consumptionHistoryMapper.sumQuantityByMaterialIdAndDateRange(
                    material.getId(), startDate, endDate);
            if (totalConsumption == null) {
                totalConsumption = 0;
            }

            // 计算年消耗金额 = 总消耗量 × 单价
            BigDecimal consumptionAmount = material.getUnitPrice() != null ?
                    material.getUnitPrice().multiply(BigDecimal.valueOf(totalConsumption)) :
                    BigDecimal.ZERO;

            totalAmount = totalAmount.add(consumptionAmount);

            // 创建可分类对象
            AbcClassifiableMaterial abcMat = new AbcClassifiableMaterial(material);
            // 临时设置计算出的消耗金额（用于排序）
            abcMat.setCalculatedAmount(consumptionAmount);
            abcMaterials.add(abcMat);
        }

        // 按消耗金额降序排序
        abcMaterials.sort((m1, m2) -> m2.getAnnualConsumptionAmount().compareTo(m1.getAnnualConsumptionAmount()));

        // 计算累计百分比并分类
        List<AbcClassifiableMaterial> classA = new ArrayList<>();
        List<AbcClassifiableMaterial> classB = new ArrayList<>();
        List<AbcClassifiableMaterial> classC = new ArrayList<>();

        BigDecimal cumulativeAmount = BigDecimal.ZERO;

        for (AbcClassifiableMaterial mat : abcMaterials) {
            cumulativeAmount = cumulativeAmount.add(mat.getAnnualConsumptionAmount());
            BigDecimal cumulativePercent = totalAmount.compareTo(BigDecimal.ZERO) > 0 ?
                    cumulativeAmount.divide(totalAmount, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            if (cumulativePercent.doubleValue() <= abcClassAPercent) {
                classA.add(mat);
                mat.setAbcClass("A");
            } else if (cumulativePercent.doubleValue() <= abcClassAPercent + abcClassBPercent) {
                classB.add(mat);
                mat.setAbcClass("B");
            } else {
                classC.add(mat);
                mat.setAbcClass("C");
            }
        }

        // 更新数据库中的ABC分类
        for (AbcClassifiableMaterial mat : classA) {
            materialMapper.updateAbcClass(mat.getMaterial().getId(), "A");
        }
        for (AbcClassifiableMaterial mat : classB) {
            materialMapper.updateAbcClass(mat.getMaterial().getId(), "B");
        }
        for (AbcClassifiableMaterial mat : classC) {
            materialMapper.updateAbcClass(mat.getMaterial().getId(), "C");
        }

        // 转换为扁平化结果
        List<AlgorithmResultVO.AbcClassificationFlatVO> flatResults = new ArrayList<>();
        BigDecimal cumulativeForCalc = BigDecimal.ZERO;

        for (AbcClassifiableMaterial mat : abcMaterials) {
            AlgorithmResultVO.AbcClassificationFlatVO vo = new AlgorithmResultVO.AbcClassificationFlatVO();
            Material m = mat.getMaterial();
            vo.setMaterialId(m.getId());
            vo.setMaterialName(m.getName());
            vo.setMaterialCode(m.getCode());
            vo.setAnnualConsumptionAmount(mat.getAnnualConsumptionAmount());
            vo.setConsumptionAmount(mat.getAnnualConsumptionAmount());

            cumulativeForCalc = cumulativeForCalc.add(mat.getAnnualConsumptionAmount());
            BigDecimal cumulativePercent = totalAmount.compareTo(BigDecimal.ZERO) > 0 ?
                    cumulativeForCalc.divide(totalAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) :
                    BigDecimal.ZERO;
            vo.setCumulativePercentage(cumulativePercent);
            vo.setAbcClass(mat.getMaterial().getAbcClass());
            vo.setManagementStrategy(getManagementStrategy(mat.getMaterial().getAbcClass()));

            flatResults.add(vo);
        }

        return flatResults;
    }

    @Override
    public List<AlgorithmResultVO.DemandForecastFlatVO> forecastDemandFlat(Integer period) {
        List<AlgorithmResultVO.DemandForecastResult> results = forecastDemand(period);
        List<AlgorithmResultVO.DemandForecastFlatVO> flatResults = new ArrayList<>();
        for (AlgorithmResultVO.DemandForecastResult r : results) {
            flatResults.add(AlgorithmResultConverter.convertDemandForecast(r));
        }
        return flatResults;
    }

    @Override
    public List<AlgorithmResultVO.ReorderPointFlatVO> calculateReorderPointsFlat() {
        List<AlgorithmResultVO.ReorderPointResult> results = calculateReorderPoints();
        List<AlgorithmResultVO.ReorderPointFlatVO> flatResults = new ArrayList<>();
        for (AlgorithmResultVO.ReorderPointResult r : results) {
            flatResults.add(AlgorithmResultConverter.convertReorderPoint(r));
        }
        return flatResults;
    }

    @Override
    public List<AlgorithmResultVO.AnomalyDetectionFlatVO> detectAnomaliesFlat(LocalDate date) {
        List<AlgorithmResultVO.AnomalyDetectionResult> results = detectAnomalies(date);
        List<AlgorithmResultVO.AnomalyDetectionFlatVO> flatResults = new ArrayList<>();
        for (AlgorithmResultVO.AnomalyDetectionResult r : results) {
            flatResults.add(AlgorithmResultConverter.convertAnomalyDetection(r));
        }
        return flatResults;
    }

    @Override
    public AlgorithmResultVO.AnomalyDetectionFlatVO checkApplicationAnomaly(
            Long userId, Long materialId, Integer quantity) {
        // 查询该物资的历史申领记录（按物资维度，APPROVED/OUTBOUND状态）
        List<Application> history = applicationMapper.selectMaterialHistory(materialId);

        AlgorithmResultVO.AnomalyDetectionFlatVO vo = new AlgorithmResultVO.AnomalyDetectionFlatVO();
        vo.setUserId(userId);
        vo.setMaterialId(materialId);
        vo.setCurrentQuantity(quantity);

        if (history.size() >= 2) {
            List<Integer> quantities = history.stream()
                    .map(Application::getQuantity)
                    .collect(Collectors.toList());

            double mean = quantities.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            double variance = quantities.stream()
                    .mapToDouble(q -> Math.pow(q - mean, 2))
                    .average().orElse(0.0);
            double stdDev = Math.sqrt(variance);

            BigDecimal historicalMean = BigDecimal.valueOf(mean);
            BigDecimal historicalStdDev = BigDecimal.valueOf(stdDev);
            BigDecimal threshold = historicalMean.add(historicalStdDev.multiply(BigDecimal.valueOf(abnormalSigmaMultiple)));

            vo.setHistoricalMean(historicalMean);
            vo.setHistoricalStdDev(historicalStdDev);
            vo.setThreshold(threshold);

            boolean isAnomaly = BigDecimal.valueOf(quantity).compareTo(threshold) > 0;
            vo.setIsAnomaly(isAnomaly);

            if (isAnomaly) {
                vo.setAnomalyType("HIGH");
                vo.setDetectedValue(BigDecimal.valueOf(quantity));
                vo.setSuggestedAction("建议审批人仔细核实该申领，可能存在异常");

                if (mean > 0) {
                    BigDecimal deviation = BigDecimal.valueOf(quantity)
                        .subtract(historicalMean)
                        .divide(historicalMean, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                    vo.setDeviation(deviation.setScale(2, RoundingMode.HALF_UP));
                }

                BigDecimal lower = historicalMean.subtract(historicalStdDev);
                BigDecimal upper = historicalMean.add(historicalStdDev);
                vo.setNormalRange(lower.setScale(2, RoundingMode.HALF_UP) + " ~ " +
                                 upper.setScale(2, RoundingMode.HALF_UP));
            }
        } else if (history.size() == 1) {
            // 只有1条历史记录，只能计算均值
            double mean = history.get(0).getQuantity();
            BigDecimal historicalMean = BigDecimal.valueOf(mean);
            vo.setHistoricalMean(historicalMean);
            vo.setHistoricalStdDev(BigDecimal.ZERO);
            vo.setThreshold(null);
            vo.setIsAnomaly(false);
        } else {
            // 没有历史记录
            vo.setIsAnomaly(false);
        }

        Material material = materialMapper.selectById(materialId);
        if (material != null) {
            vo.setMaterialName(material.getName());
            vo.setMaterialCode(material.getCode());
        }
        User user = userMapper.selectById(userId);
        if (user != null) {
            vo.setUserName(user.getRealName());
        }
        vo.setDetectionTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return vo;
    }

    /**
     * 根据ABC分类获取管理策略
     */
    private String getManagementStrategy(String abcClass) {
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
}