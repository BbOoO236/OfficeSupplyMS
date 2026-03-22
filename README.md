# 企业办公室物资申领管理系统

## 项目简介

一个完整的企业办公室物资申领管理系统，包含前后端分离架构、角色权限管理、物资申领流程、库存管理、智能算法分析和数据可视化报表等功能。

## 技术栈

### 后端
- Spring Boot 2.7.18
- MyBatis Plus 3.5.3.1
- MySQL 8.0
- JWT 身份认证
- Quartz 定时任务

### 前端
- Vue.js 2.6.14
- Element UI 2.15.14
- Vue Router 3.5.1
- Vuex 3.6.2
- ECharts 5.4.1
- Axios 0.27.2

### 算法模块
- ABC分类法
- 移动平均法需求预测
- 补货点计算
- 异常检测（3σ原则）

## 功能模块

### 1. 基础信息管理（仅管理员）
- 用户管理：增删改查用户，分配角色（普通用户/管理员）
- 物资档案管理：增删改查物资，包含物资名称、类别、规格、单价、采购提前期、安全系数、当前库存、ABC分类等级等字段

### 2. 物资申领流程
- 普通用户发起申领单：选择物资、填写数量、用途，提交后状态为"待审批"
- 管理员审批：查看所有待审批申领单，可批准或驳回，批准后状态变为"待出库"
- 管理员出库：对"待出库"的申领单进行出库操作，扣减库存，完成申领闭环
- 普通用户可查看自己发起的申领记录及审批状态
- 管理员可查看所有申领记录

### 3. 库存核心管理（仅管理员）
- 入库管理：入库单登记，库存增加
- 出库管理：除申领出库外，也支持直接出库操作（如报废）
- 库存盘点：支持盘点调整，生成盘点差异单，修正实际库存
- 动态预警：当库存低于算法计算出的补货点时，自动标红提醒

### 4. 智能算法分析
- 物资分类（ABC分类法）：根据每种物资的年消耗金额占比自动分类
- 需求预测（移动平均法）：基于历史申领数据预测下个月的物资消耗量
- 补货点计算：动态计算安全库存 = 日均消耗量 × 采购提前期 × 安全系数
- 异常检测（3σ原则）：当普通用户提交申领时，自动检测是否为异常申领

### 5. 数据可视化报表（仅管理员）
- 库存周转率趋势图（按月）
- 各物资申领数量TOP10柱状图
- ABC分类占比饼图
- 每月申领单数量与金额趋势折线图
- 预测与实际消耗对比图（折线图）

## 系统架构

```
office-supply-ms/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/com/officesupplyms/
│   │   ├── config/         # 配置类
│   │   ├── controller/     # 控制器层
│   │   ├── service/        # 服务接口层
│   │   ├── service/impl/   # 服务实现层
│   │   ├── mapper/         # 数据访问层
│   │   ├── model/          # 数据模型
│   │   │   ├── entity/     # 实体类
│   │   │   ├── dto/        # 数据传输对象
│   │   │   ├── vo/         # 视图对象
│   │   │   └── query/      # 查询对象
│   │   ├── utils/          # 工具类
│   │   ├── algorithm/      # 算法模块
│   │   └── OfficeSupplyManagementSystemApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml # 配置文件
│   │   └── mapper/         # MyBatis XML 映射文件
│   └── pom.xml
├── frontend/               # Vue.js 前端
│   ├── public/             # 静态资源
│   ├── src/
│   │   ├── api/            # API接口
│   │   ├── assets/         # 静态资源
│   │   ├── components/     # 公共组件
│   │   ├── layout/         # 布局组件
│   │   ├── router/         # 路由配置
│   │   ├── store/          # Vuex状态管理
│   │   ├── utils/          # 工具函数
│   │   ├── views/          # 页面视图
│   │   ├── App.vue         # 根组件
│   │   └── main.js         # 入口文件
│   ├── package.json
│   └── vue.config.js
├── database.sql            # 数据库脚本
└── README.md              # 项目说明
```

## 数据库设计

### 核心表结构
1. **用户表 (user)**: 存储用户信息，包括用户名、密码、角色、部门等
2. **物资表 (material)**: 存储物资档案信息，包括编码、名称、类别、单价、库存等
3. **申领单表 (application)**: 存储物资申领记录，包括申请人、物资、数量、状态等
4. **入库单表 (stock_in)**: 存储入库记录
5. **出库单表 (stock_out)**: 存储出库记录
6. **库存盘点表 (inventory_check)**: 存储盘点记录
7. **历史消耗记录表 (consumption_history)**: 用于需求预测和异常检测
8. **预警记录表 (alert_record)**: 存储预警信息

## 部署步骤

### 环境要求
- JDK 11+
- MySQL 8.0+
- Node.js 14+ (前端)
- Maven 3.6+ (后端)

### 1. 数据库部署
```sql
-- 创建数据库
CREATE DATABASE office_supply_ms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 执行建表脚本
mysql -u root -p office_supply_ms < database.sql
```

### 2. 后端部署
```bash
# 进入后端目录
cd backend

# 修改数据库配置（application.yml）
# 根据实际情况修改数据库连接信息

# 使用Maven打包
mvn clean package -DskipTests

# 运行jar包
java -jar target/office-supply-management-system-1.0.0.jar

# 或者使用Maven直接运行
mvn spring-boot:run
```

### 3. 前端部署
```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 开发环境运行
npm run serve

# 生产环境构建
npm run build
```

### 4. 访问地址
- 前端应用: http://localhost:8081
- 后端API: http://localhost:8080/api

## 默认账号

### 管理员账号
- 用户名: admin
- 密码: admin123
- 角色: ADMIN

### 普通用户账号
- 用户名: user1
- 密码: user123
- 角色: USER

## 算法模块说明

### 1. ABC分类算法
- 根据物资的年消耗金额进行排序
- A类物资：累计金额占比70%-80%
- B类物资：中间10%-20%
- C类物资：最后5%-10%
- 定时任务：每天凌晨2点自动计算

### 2. 需求预测算法
- 简单移动平均法：最近N个月消耗量的平均值
- 加权移动平均法：近期权重较高
- 默认周期：3个月

### 3. 补货点计算
- 安全库存 = 日均消耗量 × 采购提前期 × 安全系数
- 补货点 = 日均消耗量 × 采购提前期 + 安全库存
- 定时任务：每小时计算一次

### 4. 异常检测算法
- 基于3σ原则（三西格玛原则）
- 计算用户对某物资的历史申领数据的均值μ和标准差σ
- 如果本次申领数量 > μ + 3σ，则判定为异常
- 实时检测：用户提交申领时自动检测

## API接口文档

### 认证相关
- POST /api/auth/login - 用户登录
- POST /api/auth/logout - 用户注销
- GET /api/auth/current - 获取当前用户信息

### 用户管理
- GET /api/users - 查询所有用户
- POST /api/users - 创建用户
- GET /api/users/{id} - 根据ID查询用户
- PUT /api/users/{id} - 更新用户
- DELETE /api/users/{id} - 删除用户

### 物资管理
- GET /api/materials - 查询所有物资
- POST /api/materials - 创建物资
- GET /api/materials/{id} - 根据ID查询物资
- PUT /api/materials/{id} - 更新物资
- DELETE /api/materials/{id} - 删除物资

### 申领管理
- GET /api/applications/my - 查询我的申领记录
- POST /api/applications - 创建申领单
- PUT /api/applications/approve - 审批申领单
- PUT /api/applications/{id}/outbound - 出库操作

### 算法分析
- GET /api/algorithms/abc-classification - ABC分类计算
- GET /api/algorithms/demand-forecast - 需求预测
- GET /api/algorithms/reorder-points - 补货点计算
- GET /api/algorithms/anomaly-detection - 异常检测

## 配置说明

### 后端配置 (application.yml)
```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/office_supply_ms
    username: root
    password: 123456

app:
  jwt:
    secret: office-supply-management-system-secret-key-2026
    expiration: 86400000 # 24小时
```

### 前端配置 (vue.config.js)
```javascript
devServer: {
  port: 8081,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

## 常见问题

### 1. 数据库连接失败
- 检查MySQL服务是否启动
- 确认数据库连接配置正确
- 检查数据库用户权限

### 2. 前端无法访问后端API
- 确认后端服务已启动
- 检查代理配置是否正确
- 查看浏览器控制台错误信息

### 3. 定时任务不执行
- 检查Quartz配置
- 确认数据库中有QRTZ_开头的表
- 查看应用日志

### 4. 算法计算结果不准确
- 确认有足够的历史数据
- 检查算法参数配置
- 查看物资的年消耗数据是否已更新

## 开发说明

### 后端开发
1. 实体类位于 `model/entity` 包
2. DTO类位于 `model/dto` 包，用于接收前端数据
3. VO类位于 `model/vo` 包，用于返回给前端的数据
4. 业务逻辑写在 `service/impl` 包
5. 数据访问写在 `mapper` 包

### 前端开发
1. API调用写在 `api` 目录
2. 页面组件写在 `views` 目录
3. 公共组件写在 `components` 目录
4. 路由配置在 `router/index.js`
5. 状态管理在 `store/index.js`

## 后续扩展建议

1. **消息通知**: 添加邮件、短信通知功能
2. **移动端**: 开发微信小程序或APP
3. **报表导出**: 支持Excel、PDF格式导出
4. **批量操作**: 支持批量导入、导出、审批
5. **工作流**: 集成工作流引擎，支持多级审批
6. **供应商管理**: 添加供应商管理模块
7. **采购管理**: 集成采购流程管理

## 许可证

本项目仅供学习参考，可用于企业内部管理系统开发。