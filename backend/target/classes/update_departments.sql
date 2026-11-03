-- 部门更新脚本
-- 更新为指定的8个部门（中文名称）

-- 清空现有部门数据
DELETE FROM department;

-- 插入新的部门数据（行政部、人力资源部、财务部、市场部、销售部、研发部、生产部、法务部）
INSERT INTO department (name, description, status, create_time) VALUES
('行政部', '负责公司行政事务管理', 1, NOW()),
('人力资源部', '负责公司人力资源招聘、培训、绩效管理等', 1, NOW()),
('财务部', '负责公司财务管理、会计核算、财务分析等', 1, NOW()),
('市场部', '负责公司市场推广、品牌建设、市场调研等', 1, NOW()),
('销售部', '负责公司产品销售、客户关系管理等', 1, NOW()),
('研发部', '负责公司产品研发、技术创新、产品改进等', 1, NOW()),
('生产部', '负责公司生产计划、生产调度、产品质量等', 1, NOW()),
('法务部', '负责公司法律事务、合同审核、风险控制等', 1, NOW());

-- 更新用户表中的部门名称（旧的英文或混合名称映射到新名称）
-- IT/IT部 -> 研发部
UPDATE user SET department = '研发部' WHERE department IN ('IT', 'IT部', 'Tech', '技术部');

-- Admin/综合管理部 -> 行政部
UPDATE user SET department = '行政部' WHERE department IN ('Admin', '综合管理部', 'Operations', '运营部', 'CustomerService', '客服部', 'Procurement', '采购部');

-- Finance -> 财务部 (已经是中文，保持不变)
-- HR -> 人力资源部 (已经是中文，保持不变)
-- Sales -> 销售部 (已经是中文，保持不变)
-- Marketing -> 市场部 (已经是中文，保持不变)

-- 验证结果
SELECT '部门表数据:' AS '';
SELECT id, name, description FROM department;

SELECT '用户表部门分布:' AS '';
SELECT department, COUNT(*) as user_count FROM user GROUP BY department;
