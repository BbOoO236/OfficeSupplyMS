# 算法模块前后端集成设计

## 1. 背景

企业办公室物资管理系统的4个算法模块（需求预测、ABC分类、异常检测、动态补货点）已完成后端实现，前端页面也已搭建。但**后端返回数据模型与前端期望的字段不匹配**，导致数据无法正确展示。

## 2. 现状分析

### 2.1 后端已实现
- `AlgorithmService` - 算法业务逻辑
- `AlgorithmController` - REST API接口
- `AlgorithmUtil` - 算法工具类
- 已有VO: `AlgorithmResultVO` (内部嵌套结构)

### 2.2 前端已实现
- 4个Vue组件: ABC.vue, Forecast.vue, Reorder.vue, Anomaly.vue
- API调用层: algorithm.js

### 2.3 数据不匹配问题

| 模块 | 后端返回字段 | 前端期望字段 |
|------|------------|-------------|
| ABC | classA/classB/classC (嵌套列表) | 扁平列表 + consumptionAmount + cumulativePercentage + managementStrategy |
| Forecast | simpleMovingAverage, weightedMovingAverage | historicalAvg, forecastAvg, forecastTotal, confidenceInterval, recommendedPurchase |
| Reorder | currentStock, safetyStock, reorderPoint, needReorder | 上述 + stockStatus (LOW/NORMAL/HIGH) |
| Anomaly | applicationId, userId, materialId, currentQuantity, isAnomaly | materialName, materialCode, anomalyType, detectedValue, normalRange, deviation, suggestedAction |

## 3. 设计方案

### 3.1 总体架构

```
Controller -> Service -> AlgorithmUtil (算法计算)
                |
                v
        Converter (数据格式转换)  <-- 新增
                |
                v
        前端期望的扁平化VO
```

### 3.2 新增文件

#### 3.2.1 扁平化VO类 (AlgorithmResultVO 新增内部类)

```java
// ABC分类扁平化结果
@Data
public static class AbcClassificationFlatVO {
    private Long materialId;
    private String materialName;
    private String materialCode;
    private BigDecimal annualConsumptionAmount;  // 年消耗金额
    private BigDecimal cumulativePercentage;     // 累计百分比
    private String abcClass;                     // A/B/C
    private String managementStrategy;           // 管理策略
}

// 需求预测扁平化结果
@Data
public static class DemandForecastFlatVO {
    private Long materialId;
    private String materialName;
    private String materialCode;
    private BigDecimal historicalAvg;            // 历史平均月消耗
    private BigDecimal forecastAvg;              // 预测月消耗
    private BigDecimal forecastTotal;            // 预测总需求
    private String confidenceInterval;           // 置信区间
    private BigDecimal recommendedPurchase;      // 建议采购量
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
    private String stockStatus;                  // LOW/NORMAL/HIGH
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
    private String anomalyType;                 // HIGH/LOW
    private BigDecimal detectedValue;
    private String normalRange;
    private BigDecimal deviation;
    private String suggestedAction;
    private String detectionTime;
}
```

#### 3.2.2 数据转换器 (AlgorithmResultConverter)

新增 `AlgorithmResultConverter` 类，负责：
1. ABC分类：扁平化 + 补充字段（累计占比、管理策略）
2. 需求预测：字段映射 + 计算总需求和置信区间
3. 补货点：计算库存状态
4. 异常检测：联查物资名称 + 计算异常类型和建议

### 3.3 接口调整

| 接口 | 方法 | 返回类型 |
|------|------|---------|
| GET /algorithms/abc-classification | calculateAbcClassification | `Result<List<AbcClassificationFlatVO>>` |
| GET /algorithms/demand-forecast | forecastDemand | `Result<List<DemandForecastFlatVO>>` |
| GET /algorithms/reorder-points | calculateReorderPoints | `Result<List<ReorderPointFlatVO>>` |
| GET /algorithms/anomaly-detection | detectAnomalies | `Result<List<AnomalyDetectionFlatVO>>` |

### 3.4 申领提交异常检测（实时校验）

在 `ApplicationController.submitApplication` 中：
1. 提交前调用 `algorithmService.calculateUserMaterialStatistics(userId, materialId)`
2. 判断本次申领数量是否超过阈值（mean + 3*std）
3. 返回异常标记给前端，但**不阻断提交**

## 4. 字段转换规则

### 4.1 ABC分类
- **consumptionAmount**: `material.annualConsumptionAmount`
- **cumulativePercentage**: 根据物资在排序后的位置计算
- **managementStrategy**:
  - A类: "重点管理，优先补货"
  - B类: "常规管理，定期盘点"
  - C类: "简化管理，按需采购"

### 4.2 需求预测
- **historicalAvg**: `simpleMovingAverage`
- **forecastAvg**: `weightedMovingAverage`
- **forecastTotal**: `weightedMovingAverage * 3` (默认3个月预测)
- **confidenceInterval**: `预测值±标准差`
- **recommendedPurchase**: `forecastTotal - currentStock` (如为正数)

### 4.3 补货点
- **stockStatus**:
  - `currentStock < safetyStock` → LOW
  - `currentStock > maxStock` → HIGH
  - 其他 → NORMAL

### 4.4 异常检测
- **anomalyType**: `isAnomaly ? HIGH : null`
- **detectedValue**: `currentQuantity`
- **normalRange**: `historicalMean ± historicalStdDev`
- **deviation**: `((currentQuantity - historicalMean) / historicalMean) * 100`
- **suggestedAction**:
  - HIGH: "建议审批人仔细核实该申领"
  - LOW: "关注消耗数据，可能存在浪费"

## 5. 数据库变更

无需数据库变更。所有计算结果基于现有表：
- `material` - 已有 abc_class, annual_consumption 等字段
- `consumption_history` - 历史消耗数据
- `application` - 申领记录

## 6. 实现顺序

1. 新增扁平化VO类
2. 实现 AlgorithmResultConverter 转换器
3. 修改 Controller 返回类型
4. 调整 Service 层调用转换器
5. 实现申领提交时的实时异常检测
6. 前端适配（如需要字段微调）

## 7. 测试要点

1. ABC分类：验证A/B/C占比是否正确
2. 需求预测：用已知历史数据验证预测值
3. 补货点：验证 LOW/NORMAL/HIGH 状态判断
4. 异常检测：提交大额申领验证异常标记

---

**设计完成时间**: 2026-04-22
