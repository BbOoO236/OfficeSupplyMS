# 算法模块前后端集成实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复后端返回数据模型与前端期望字段的不匹配，实现4个算法模块的前后端完全打通

**Architecture:** 在Service层与Controller层之间增加数据转换层(AlgorithmResultConverter)，将后端嵌套结构转为前端期望的扁平结构。申领提交时增加实时异常检测。

**Tech Stack:** Spring Boot 2.7.18, MyBatis Plus, Vue, Element UI

---

## 文件结构

```
backend/src/main/java/com/officesupplyms/
├── model/vo/AlgorithmResultVO.java          # 修改：新增4个扁平化内部类
├── converter/AlgorithmResultConverter.java  # 新建：数据转换器
├── service/AlgorithmService.java            # 修改：接口方法返回类型
├── service/impl/AlgorithmServiceImpl.java   # 修改：实现转换逻辑
├── controller/AlgorithmController.java      # 修改：返回类型
└── controller/ApplicationController.java     # 修改：申领提交增加异常检测
```

---

## Task 1: 新增扁平化VO类

**Files:**
- Modify: `backend/src/main/java/com/officesupplyms/model/vo/AlgorithmResultVO.java`

- [ ] **Step 1: 在 AlgorithmResultVO.java 中新增扁平化内部类**

在文件末尾 `}` 之前添加以下4个内部类：

```java
// ABC分类扁平化结果
@Data
public static class AbcClassificationFlatVO {
    private Long materialId;
    private String materialName;
    private String materialCode;
    private BigDecimal annualConsumptionAmount;
    private BigDecimal cumulativePercentage;
    private String abcClass;
    private String managementStrategy;
}

// 需求预测扁平化结果
@Data
public static class DemandForecastFlatVO {
    private Long materialId;
    private String materialName;
    private String materialCode;
    private BigDecimal historicalAvg;
    private BigDecimal forecastAvg;
    private BigDecimal forecastTotal;
    private String confidenceInterval;
    private BigDecimal recommendedPurchase;
}

// 补货点扁平化结果
@Data
public static class ReorderPointFlatVO {
    private Long materialId;
    private String materialName;
    private String materialCode;
    private Integer currentStock;
    private BigDecimal dailyConsumption;
    private Integer leadTime;
    private BigDecimal safetyFactor;
    private Integer safetyStock;
    private Integer reorderPoint;
    private Boolean needReorder;
    private String stockStatus;
}

// 异常检测扁平化结果
@Data
public static class AnomalyDetectionFlatVO {
    private Long applicationId;
    private String applicationNo;
    private Long userId;
    private String userName;
    private Long materialId;
    private String materialName;
    private String materialCode;
    private Integer currentQuantity;
    private BigDecimal historicalMean;
    private BigDecimal historicalStdDev;
    private BigDecimal threshold;
    private Boolean isAnomaly;
    private String anomalyType;
    private BigDecimal detectedValue;
    private String normalRange;
    private BigDecimal deviation;
    private String suggestedAction;
    private String detectionTime;
}
```

- [ ] **Step 2: 验证编译**

Run: `cd backend && mvn compile -DskipTests`
Expected: BUILD SUCCESS

---

## Task 2: 创建数据转换器

**Files:**
- Create: `backend/src/main/java/com/officesupplyms/converter/AlgorithmResultConverter.java`

- [ ] **Step 1: 创建 AlgorithmResultConverter.java**

```java
package com.officesupplyms.converter;

import com.officesupplyms.model.entity.Application;
import com.officesupplyms.model.entity.Material;
import com.officesupplyms.model.entity.User;
import com.officesupplyms.model.vo.AlgorithmResultVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 算法结果转换器 - 将后端嵌套结构转为前端扁平结构
 */
public class AlgorithmResultConverter {

    /**
     * ABC分类结果扁平化
     */
    public static List<AbcClassificationFlatVO> convertAbcClassification(
            AlgorithmResultVO.AbcClassificationResult result,
            List<Material> allMaterials) {

        List<AbcClassificationFlatVO> flatList = new ArrayList<>();
        BigDecimal totalAmount = result.getTotalAmount();

        // 处理A类
        for (int i = 0; i < result.getClassA().size(); i++) {
            Material mat = result.getClassA().get(i);
            flatList.add(createAbcFlatVO(mat, "A", totalAmount, i + 1, result.getClassA().size()));
        }

        // 处理B类
        int bStartIndex = result.getClassA().size();
        for (int i = 0; i < result.getClassB().size(); i++) {
            Material mat = result.getClassB().get(i);
            flatList.add(createAbcFlatVO(mat, "B", totalAmount, bStartIndex + i + 1, flatList.size()));
        }

        // 处理C类
        int cStartIndex = flatList.size();
        for (int i = 0; i < result.getClassC().size(); i++) {
            Material mat = result.getClassC().get(i);
            flatList.add(createAbcFlatVO(mat, "C", totalAmount, cStartIndex + i + 1, flatList.size()));
        }

        // 按累计百分比排序
        flatList.sort((a, b) -> a.getCumulativePercentage().compareTo(b.getCumulativePercentage()));

        // 重新计算序号
        for (int i = 0; i < flatList.size(); i++) {
            flatList.get(i).setCumulativePercentage(
                flatList.get(i).getAnnualConsumptionAmount()
                    .divide(totalAmount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
            );
        }

        return flatList;
    }

    private static AbcClassificationFlatVO createAbcFlatVO(Material mat, String abcClass,
            BigDecimal totalAmount, int rank, int totalCount) {
        AbcClassificationFlatVO vo = new AbcClassificationFlatVO();
        vo.setMaterialId(mat.getId());
        vo.setMaterialName(mat.getName());
        vo.setMaterialCode(mat.getCode());
        vo.setAnnualConsumptionAmount(mat.getAnnualConsumptionAmount());
        vo.setAbcClass(abcClass);

        // 管理策略
        switch (abcClass) {
            case "A":
                vo.setManagementStrategy("重点管理，优先补货");
                break;
            case "B":
                vo.setManagementStrategy("常规管理，定期盘点");
                break;
            default:
                vo.setManagementStrategy("简化管理，按需采购");
        }

        return vo;
    }

    /**
     * 需求预测结果扁平化
     */
    public static List<DemandForecastFlatVO> convertDemandForecast(
            List<AlgorithmResultVO.DemandForecastResult> results,
            Material material) {

        List<DemandForecastFlatVO> flatList = new ArrayList<>();

        for (AlgorithmResultVO.DemandForecastResult r : results) {
            DemandForecastFlatVO vo = new DemandForecastFlatVO();
            vo.setMaterialId(r.getMaterialId());
            vo.setMaterialName(r.getMaterialName());
            vo.setMaterialCode(r.getMaterialCode());
            vo.setHistoricalAvg(r.getSimpleMovingAverage());
            vo.setForecastAvg(r.getWeightedMovingAverage());

            // 预测总需求 = 预测月消耗 × 3
            BigDecimal forecastTotal = r.getWeightedMovingAverage().multiply(BigDecimal.valueOf(3));
            vo.setForecastTotal(forecastTotal);

            // 置信区间
            if (r.getHistoricalData() != null && !r.getHistoricalData().isEmpty()) {
                BigDecimal mean = r.getSimpleMovingAverage();
                BigDecimal stdDev = calculateStdDev(r.getHistoricalData());
                String interval = mean.subtract(stdDev).setScale(2, RoundingMode.HALF_UP) + " ~ " +
                                  mean.add(stdDev).setScale(2, RoundingMode.HALF_UP);
                vo.setConfidenceInterval(interval);
            } else {
                vo.setConfidenceInterval("N/A");
            }

            // 建议采购量
            if (material != null && material.getCurrentStock() != null) {
                BigDecimal recommend = forecastTotal.subtract(BigDecimal.valueOf(material.getCurrentStock()));
                vo.setRecommendedPurchase(recommend.compareTo(BigDecimal.ZERO) > 0 ?
                    recommend.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            } else {
                vo.setRecommendedPurchase(forecastTotal);
            }

            flatList.add(vo);
        }

        return flatList;
    }

    private static BigDecimal calculateStdDev(List<AlgorithmResultVO.HistoricalData> data) {
        if (data == null || data.size() < 2) return BigDecimal.ZERO;

        BigDecimal sum = BigDecimal.ZERO;
        for (AlgorithmResultVO.HistoricalData d : data) {
            sum = sum.add(d.getConsumption());
        }
        BigDecimal mean = sum.divide(BigDecimal.valueOf(data.size()), 4, RoundingMode.HALF_UP);

        BigDecimal sumSquaredDiff = BigDecimal.ZERO;
        for (AlgorithmResultVO.HistoricalData d : data) {
            BigDecimal diff = d.getConsumption().subtract(mean);
            sumSquaredDiff = sumSquaredDiff.add(diff.multiply(diff));
        }

        BigDecimal variance = sumSquaredDiff.divide(BigDecimal.valueOf(data.size() - 1), 8, RoundingMode.HALF_UP);
        return sqrt(variance, 4);
    }

    private static BigDecimal sqrt(BigDecimal value, int scale) {
        if (value.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
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
     * 补货点结果扁平化
     */
    public static List<ReorderPointFlatVO> convertReorderPoints(
            List<AlgorithmResultVO.ReorderPointResult> results) {

        List<ReorderPointFlatVO> flatList = new ArrayList<>();

        for (AlgorithmResultVO.ReorderPointResult r : results) {
            ReorderPointFlatVO vo = new ReorderPointFlatVO();
            vo.setMaterialId(r.getMaterialId());
            vo.setMaterialName(r.getMaterialName());
            vo.setMaterialCode(r.getMaterialCode());
            vo.setCurrentStock(r.getCurrentStock());
            vo.setDailyConsumption(r.getDailyConsumption());
            vo.setLeadTime(r.getLeadTime());
            vo.setSafetyFactor(r.getSafetyFactor());
            vo.setSafetyStock(r.getSafetyStock());
            vo.setReorderPoint(r.getReorderPoint());
            vo.setNeedReorder(r.getNeedReorder());

            // 计算库存状态
            String status;
            if (r.getCurrentStock() < r.getSafetyStock()) {
                status = "LOW";
            } else if (r.getCurrentStock() > r.getSafetyStock() * 2) {
                status = "HIGH";
            } else {
                status = "NORMAL";
            }
            vo.setStockStatus(status);

            flatList.add(vo);
        }

        return flatList;
    }

    /**
     * 异常检测结果扁平化
     */
    public static List<AnomalyDetectionFlatVO> convertAnomalyDetection(
            List<AlgorithmResultVO.AnomalyDetectionResult> results,
            Material material, User user) {

        List<AnomalyDetectionFlatVO> flatList = new ArrayList<>();

        for (AlgorithmResultVO.AnomalyDetectionResult r : results) {
            AnomalyDetectionFlatVO vo = new AnomalyDetectionFlatVO();
            vo.setApplicationId(r.getApplicationId());
            vo.setUserId(r.getUserId());
            vo.setMaterialId(r.getMaterialId());

            if (material != null) {
                vo.setMaterialName(material.getName());
                vo.setMaterialCode(material.getCode());
            }
            if (user != null) {
                vo.setUserName(user.getName());
            }

            vo.setCurrentQuantity(r.getCurrentQuantity());
            vo.setHistoricalMean(r.getHistoricalMean());
            vo.setHistoricalStdDev(r.getHistoricalStdDev());
            vo.setThreshold(r.getThreshold());
            vo.setIsAnomaly(r.getIsAnomaly());
            vo.setDetectionTime(r.getDetectionTime());

            if (r.getIsAnomaly() != null && r.getIsAnomaly()) {
                vo.setAnomalyType("HIGH");
                vo.setDetectedValue(BigDecimal.valueOf(r.getCurrentQuantity()));
                vo.setSuggestedAction("建议审批人仔细核实该申领");

                // 计算正常范围
                if (r.getHistoricalMean() != null && r.getHistoricalStdDev() != null) {
                    BigDecimal lower = r.getHistoricalMean().subtract(r.getHistoricalStdDev());
                    BigDecimal upper = r.getHistoricalMean().add(r.getHistoricalStdDev());
                    vo.setNormalRange(lower.setScale(2, RoundingMode.HALF_UP) + " ~ " +
                                     upper.setScale(2, RoundingMode.HALF_UP));

                    // 计算偏离程度
                    if (r.getHistoricalMean().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal deviation = BigDecimal.valueOf(r.getCurrentQuantity())
                            .subtract(r.getHistoricalMean())
                            .divide(r.getHistoricalMean(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                        vo.setDeviation(deviation.setScale(2, RoundingMode.HALF_UP));
                    }
                }
            }

            flatList.add(vo);
        }

        return flatList;
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `cd backend && mvn compile -DskipTests`
Expected: BUILD SUCCESS

---

## Task 3: 修改 AlgorithmService 接口

**Files:**
- Modify: `backend/src/main/java/com/officesupplyms/service/AlgorithmService.java`

- [ ] **Step 1: 在 AlgorithmService.java 接口中添加新方法声明**

在文件末尾 `}` 之前添加：

```java
/**
 * ABC分类计算 - 返回扁平化结果
 */
List<AlgorithmResultVO.AbcClassificationFlatVO> calculateAbcClassificationFlat();

/**
 * 需求预测 - 返回扁平化结果
 */
List<AlgorithmResultVO.DemandForecastFlatVO> forecastDemandFlat(Integer period);

/**
 * 补货点计算 - 返回扁平化结果
 */
List<AlgorithmResultVO.ReorderPointFlatVO> calculateReorderPointsFlat();

/**
 * 异常检测 - 返回扁平化结果
 */
List<AlgorithmResultVO.AnomalyDetectionFlatVO> detectAnomaliesFlat(LocalDate date);

/**
 * 校验申领是否异常（实时）
 */
AlgorithmResultVO.AnomalyDetectionFlatVO checkApplicationAnomaly(Long userId, Long materialId, Integer quantity);
```

- [ ] **Step 2: 验证编译**

Run: `cd backend && mvn compile -DskipTests`
Expected: BUILD SUCCESS

---

## Task 4: 修改 AlgorithmServiceImpl 实现类

**Files:**
- Modify: `backend/src/main/java/com/officesupplyms/service/impl/AlgorithmServiceImpl.java`

需要添加：
1. 导入 MaterialMapper, UserMapper
2. 实现新接口方法

- [ ] **Step 1: 添加导入**

在文件顶部导入部分添加：
```java
import com.officesupplyms.mapper.UserMapper;
import com.officesupplyms.model.entity.User;
```

- [ ] **Step 2: 在 AlgorithmServiceImpl 类中添加Autowired**

在类中添加：
```java
@Autowired
private UserMapper userMapper;
```

- [ ] **Step 3: 在 AlgorithmServiceImpl 末尾添加新方法实现**

在类末尾 `}` 之前添加：

```java
@Override
public List<AlgorithmResultVO.AbcClassificationFlatVO> calculateAbcClassificationFlat() {
    AlgorithmResultVO.AbcClassificationResult result = calculateAbcClassification();
    List<Material> allMaterials = materialMapper.selectList(null);
    return AlgorithmResultConverter.convertAbcClassification(result, allMaterials);
}

@Override
public List<AlgorithmResultVO.DemandForecastFlatVO> forecastDemandFlat(Integer period) {
    List<AlgorithmResultVO.DemandForecastResult> results = forecastDemand(period);
    List<AlgorithmResultVO.DemandForecastFlatVO> flatResults = new ArrayList<>();
    for (AlgorithmResultVO.DemandForecastResult r : results) {
        Material material = materialMapper.selectById(r.getMaterialId());
        flatResults.addAll(AlgorithmResultConverter.convertDemandForecast(
            Collections.singletonList(r), material));
    }
    return flatResults;
}

@Override
public List<AlgorithmResultVO.ReorderPointFlatVO> calculateReorderPointsFlat() {
    List<AlgorithmResultVO.ReorderPointResult> results = calculateReorderPoints();
    return AlgorithmResultConverter.convertReorderPoints(results);
}

@Override
public List<AlgorithmResultVO.AnomalyDetectionFlatVO> detectAnomaliesFlat(LocalDate date) {
    List<AlgorithmResultVO.AnomalyDetectionResult> results = detectAnomalies(date);
    List<AlgorithmResultVO.AnomalyDetectionFlatVO> flatResults = new ArrayList<>();
    for (AlgorithmResultVO.AnomalyDetectionResult r : results) {
        Material material = materialMapper.selectById(r.getMaterialId());
        User user = userMapper.selectById(r.getUserId());
        flatResults.addAll(AlgorithmResultConverter.convertAnomalyDetection(
            Collections.singletonList(r), material, user));
    }
    return flatResults;
}

@Override
public AlgorithmResultVO.AnomalyDetectionFlatVO checkApplicationAnomaly(
        Long userId, Long materialId, Integer quantity) {
    AlgorithmResultVO.AnomalyDetectionResult stat = calculateUserMaterialStatistics(userId, materialId);

    AnomalyDetectionFlatVO vo = new AnomalyDetectionFlatVO();
    vo.setUserId(userId);
    vo.setMaterialId(materialId);
    vo.setCurrentQuantity(quantity);
    vo.setHistoricalMean(stat.getHistoricalMean());
    vo.setHistoricalStdDev(stat.getHistoricalStdDev());
    vo.setThreshold(stat.getThreshold());

    if (stat.getThreshold() != null) {
        boolean isAnomaly = BigDecimal.valueOf(quantity).compareTo(stat.getThreshold()) > 0;
        vo.setIsAnomaly(isAnomaly);

        if (isAnomaly) {
            vo.setAnomalyType("HIGH");
            vo.setDetectedValue(BigDecimal.valueOf(quantity));
            vo.setSuggestedAction("建议审批人仔细核实该申领，可能存在异常");

            if (stat.getHistoricalMean() != null && stat.getHistoricalMean().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal deviation = BigDecimal.valueOf(quantity)
                    .subtract(stat.getHistoricalMean())
                    .divide(stat.getHistoricalMean(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                vo.setDeviation(deviation.setScale(2, RoundingMode.HALF_UP));
            }

            if (stat.getHistoricalMean() != null && stat.getHistoricalStdDev() != null) {
                BigDecimal lower = stat.getHistoricalMean().subtract(stat.getHistoricalStdDev());
                BigDecimal upper = stat.getHistoricalMean().add(stat.getHistoricalStdDev());
                vo.setNormalRange(lower.setScale(2, RoundingMode.HALF_UP) + " ~ " +
                                 upper.setScale(2, RoundingMode.HALF_UP));
            }
        }
    } else {
        vo.setIsAnomaly(false);
    }

    Material material = materialMapper.selectById(materialId);
    if (material != null) {
        vo.setMaterialName(material.getName());
        vo.setMaterialCode(material.getCode());
    }
    User user = userMapper.selectById(userId);
    if (user != null) {
        vo.setUserName(user.getName());
    }
    vo.setDetectionTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

    return vo;
}
```

同时在文件顶部添加 Collections 导入：
```java
import java.util.Collections;
```

- [ ] **Step 3: 验证编译**

Run: `cd backend && mvn compile -DskipTests`
Expected: BUILD SUCCESS

---

## Task 5: 修改 AlgorithmController

**Files:**
- Modify: `backend/src/main/java/com/officesupplyms/controller/AlgorithmController.java`

- [ ] **Step 1: 修改 Controller 返回类型**

将以下接口的返回类型从嵌套结构改为扁平结构：

1. `calculateAbcClassification` 返回类型改为 `Result<List<AlgorithmResultVO.AbcClassificationFlatVO>>`
2. `forecastDemand` 返回类型改为 `Result<List<AlgorithmResultVO.DemandForecastFlatVO>>`
3. `calculateReorderPoints` 返回类型改为 `Result<List<AlgorithmResultVO.ReorderPointFlatVO>>`
4. `detectAnomalies` 返回类型改为 `Result<List<AlgorithmResultVO.AnomalyDetectionFlatVO>>`

修改后方法实现改为调用 `*Flat()` 方法：

```java
@GetMapping("/abc-classification")
public Result<List<AlgorithmResultVO.AbcClassificationFlatVO>> calculateAbcClassification() {
    return Result.success(algorithmService.calculateAbcClassificationFlat());
}

@GetMapping("/demand-forecast")
public Result<List<AlgorithmResultVO.DemandForecastFlatVO>> forecastDemand(
        @RequestParam(defaultValue = "3") Integer period) {
    return Result.success(algorithmService.forecastDemandFlat(period));
}

@GetMapping("/reorder-points")
public Result<List<AlgorithmResultVO.ReorderPointFlatVO>> calculateReorderPoints() {
    return Result.success(algorithmService.calculateReorderPointsFlat());
}

@GetMapping("/anomaly-detection")
public Result<List<AlgorithmResultVO.AnomalyDetectionFlatVO>> detectAnomalies(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
    return Result.success(algorithmService.detectAnomaliesFlat(date));
}
```

- [ ] **Step 2: 添加新的申领异常校验接口**

在文件末尾 `}` 之前添加：

```java
/**
 * 校验申领是否异常（实时）
 */
@GetMapping("/check-application-anomaly")
public Result<AlgorithmResultVO.AnomalyDetectionFlatVO> checkApplicationAnomaly(
        @RequestParam Long userId,
        @RequestParam Long materialId,
        @RequestParam Integer quantity) {
    return Result.success(algorithmService.checkApplicationAnomaly(userId, materialId, quantity));
}
```

- [ ] **Step 3: 验证编译**

Run: `cd backend && mvn compile -DskipTests`
Expected: BUILD SUCCESS

---

## Task 6: 修改 ApplicationController 实现申领提交实时异常检测

**Files:**
- Modify: `backend/src/main/java/com/officesupplyms/controller/ApplicationController.java`

- [ ] **Step 1: 查看并修改 ApplicationController**

首先读取 ApplicationController 找到提交申领的方法（可能叫 submitApplication 或 createApplication），然后：

1. 添加 AlgorithmService 注入
2. 在提交方法中调用异常检测
3. 将异常结果放入返回的 DTO 中

添加注入：
```java
@Autowired
private AlgorithmService algorithmService;
```

在申领提交逻辑中添加异常检测（根据实际代码结构调整）：

```java
// 提交前检测异常
if (materialId != null && quantity != null) {
    AlgorithmResultVO.AnomalyDetectionFlatVO anomalyResult =
        algorithmService.checkApplicationAnomaly(userId, materialId, quantity);
    if (anomalyResult.getIsAnomaly() != null && anomalyResult.getIsAnomaly()) {
        // 将异常信息存入application的isAbnormal和abnormalReason字段
        application.setIsAbnormal(1);
        application.setAbnormalReason("申领数量[" + quantity + "]超过历史均值，偏离" +
            anomalyResult.getDeviation() + "%，建议审批人核实");
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `cd backend && mvn compile -DskipTests`
Expected: BUILD SUCCESS

---

## Task 7: 前端 algorithm.js 添加新接口

**Files:**
- Modify: `frontend/src/api/algorithm.js`

- [ ] **Step 1: 添加申领异常校验接口**

在文件末尾添加：

```javascript
export function checkApplicationAnomaly(userId, materialId, quantity) {
  return request({
    url: '/algorithms/check-application-anomaly',
    method: 'get',
    params: { userId, materialId, quantity }
  })
}
```

- [ ] **Step 2: 验证语法正确**

前端无需编译，检查文件语法即可

---

## Task 8: 整体联调测试

- [ ] **Step 1: 启动后端**

Run: `cd backend && mvn spring-boot:run`
Expected: 应用启动成功，无报错

- [ ] **Step 2: 测试ABC分类接口**

Run: `curl http://localhost:8080/api/algorithms/abc-classification`
Expected: 返回 JSON 数组，包含 materialName, materialCode, abcClass, annualConsumptionAmount, cumulativePercentage, managementStrategy 字段

- [ ] **Step 3: 测试需求预测接口**

Run: `curl http://localhost:8080/api/algorithms/demand-forecast?period=3`
Expected: 返回 JSON 数组，包含 historicalAvg, forecastAvg, forecastTotal, confidenceInterval, recommendedPurchase 字段

- [ ] **Step 4: 测试补货点接口**

Run: `curl http://localhost:8080/api/algorithms/reorder-points`
Expected: 返回 JSON 数组，包含 stockStatus 字段 (LOW/NORMAL/HIGH)

- [ ] **Step 5: 测试异常检测接口**

Run: `curl http://localhost:8080/api/algorithms/anomaly-detection?date=2026-04-22`
Expected: 返回 JSON 数组，包含 anomalyType, deviation, suggestedAction 字段

- [ ] **Step 6: 测试申领异常校验接口**

Run: `curl "http://localhost:8080/api/algorithms/check-application-anomaly?userId=1&materialId=1&quantity=100"`
Expected: 返回 JSON，包含 isAnomaly, anomalyType, deviation 字段

---

## 自检清单

- [ ] 所有新增的 VO 类字段与前端期望一致
- [ ] 转换器正确计算累计百分比、偏离度等派生字段
- [ ] Controller 返回类型正确匹配前端调用
- [ ] 申领提交时异常检测不阻断正常提交流程
- [ ] 所有接口返回 JSON 结构完整

---

**Plan created:** 2026-04-22
