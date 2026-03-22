-- 企业办公室物资申领管理系统数据库脚本
-- MySQL 8.0

-- 创建数据库
CREATE DATABASE IF NOT EXISTS office_supply_ms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE office_supply_ms;

-- 用户表
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: USER-普通用户, ADMIN-管理员',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `department` VARCHAR(100) COMMENT '部门',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 物资档案表
CREATE TABLE `material` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '物资ID',
  `code` VARCHAR(50) NOT NULL COMMENT '物资编码',
  `name` VARCHAR(100) NOT NULL COMMENT '物资名称',
  `category` VARCHAR(50) NOT NULL COMMENT '物资类别',
  `specification` VARCHAR(200) COMMENT '规格',
  `unit` VARCHAR(20) NOT NULL COMMENT '单位',
  `unit_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '单价',
  `lead_time` INT NOT NULL DEFAULT 7 COMMENT '采购提前期(天)',
  `safety_factor` DECIMAL(5,2) NOT NULL DEFAULT 1.5 COMMENT '安全系数',
  `current_stock` INT NOT NULL DEFAULT 0 COMMENT '当前库存',
  `min_stock` INT NOT NULL DEFAULT 0 COMMENT '最低库存',
  `max_stock` INT NOT NULL DEFAULT 0 COMMENT '最高库存',
  `abc_class` CHAR(1) COMMENT 'ABC分类: A, B, C',
  `annual_consumption` INT DEFAULT 0 COMMENT '年消耗量',
  `annual_consumption_amount` DECIMAL(15,2) DEFAULT 0.00 COMMENT '年消耗金额',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-正常, 0-停用',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物资档案表';

-- 申领单表
CREATE TABLE `application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申领单ID',
  `application_no` VARCHAR(50) NOT NULL COMMENT '申领单号',
  `user_id` BIGINT NOT NULL COMMENT '申请人ID',
  `material_id` BIGINT NOT NULL COMMENT '物资ID',
  `quantity` INT NOT NULL COMMENT '申领数量',
  `purpose` VARCHAR(500) NOT NULL COMMENT '用途',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待审批, APPROVED-已批准, REJECTED-已驳回, TO_BE_OUTBOUND-待出库, COMPLETED-已完成',
  `apply_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `approve_time` DATETIME COMMENT '审批时间',
  `approve_user_id` BIGINT COMMENT '审批人ID',
  `reject_reason` VARCHAR(500) COMMENT '驳回原因',
  `outbound_time` DATETIME COMMENT '出库时间',
  `outbound_user_id` BIGINT COMMENT '出库操作人ID',
  `is_abnormal` TINYINT NOT NULL DEFAULT 0 COMMENT '是否异常申领: 0-正常, 1-异常',
  `abnormal_reason` VARCHAR(200) COMMENT '异常原因',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_application_no` (`application_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_material_id` (`material_id`),
  KEY `idx_status` (`status`),
  KEY `idx_apply_time` (`apply_time`),
  CONSTRAINT `fk_application_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_application_material` FOREIGN KEY (`material_id`) REFERENCES `material` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物资申领单表';

-- 入库单表
CREATE TABLE `stock_in` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '入库单ID',
  `stock_in_no` VARCHAR(50) NOT NULL COMMENT '入库单号',
  `material_id` BIGINT NOT NULL COMMENT '物资ID',
  `quantity` INT NOT NULL COMMENT '入库数量',
  `unit_price` DECIMAL(10,2) NOT NULL COMMENT '单价',
  `total_amount` DECIMAL(15,2) NOT NULL COMMENT '总金额',
  `supplier` VARCHAR(200) COMMENT '供应商',
  `in_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
  `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stock_in_no` (`stock_in_no`),
  KEY `idx_material_id` (`material_id`),
  KEY `idx_in_time` (`in_time`),
  CONSTRAINT `fk_stock_in_material` FOREIGN KEY (`material_id`) REFERENCES `material` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单表';

-- 出库单表
CREATE TABLE `stock_out` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '出库单ID',
  `stock_out_no` VARCHAR(50) NOT NULL COMMENT '出库单号',
  `material_id` BIGINT NOT NULL COMMENT '物资ID',
  `quantity` INT NOT NULL COMMENT '出库数量',
  `type` VARCHAR(20) NOT NULL DEFAULT 'APPLICATION' COMMENT '出库类型: APPLICATION-申领出库, DIRECT-直接出库, SCRAP-报废',
  `application_id` BIGINT COMMENT '关联申领单ID',
  `out_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '出库时间',
  `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
  `reason` VARCHAR(500) COMMENT '出库原因',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stock_out_no` (`stock_out_no`),
  KEY `idx_material_id` (`material_id`),
  KEY `idx_out_time` (`out_time`),
  KEY `idx_application_id` (`application_id`),
  CONSTRAINT `fk_stock_out_material` FOREIGN KEY (`material_id`) REFERENCES `material` (`id`),
  CONSTRAINT `fk_stock_out_application` FOREIGN KEY (`application_id`) REFERENCES `application` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单表';

-- 库存盘点表
CREATE TABLE `inventory_check` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '盘点ID',
  `check_no` VARCHAR(50) NOT NULL COMMENT '盘点单号',
  `material_id` BIGINT NOT NULL COMMENT '物资ID',
  `book_quantity` INT NOT NULL COMMENT '账面数量',
  `actual_quantity` INT NOT NULL COMMENT '实际数量',
  `difference` INT NOT NULL COMMENT '差异数量',
  `check_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '盘点时间',
  `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
  `reason` VARCHAR(500) COMMENT '差异原因',
  `adjustment` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已调整: 0-未调整, 1-已调整',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_check_no` (`check_no`),
  KEY `idx_material_id` (`material_id`),
  KEY `idx_check_time` (`check_time`),
  CONSTRAINT `fk_inventory_check_material` FOREIGN KEY (`material_id`) REFERENCES `material` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点表';

-- 历史消耗记录表（用于需求预测和异常检测）
CREATE TABLE `consumption_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `material_id` BIGINT NOT NULL COMMENT '物资ID',
  `user_id` BIGINT COMMENT '用户ID（申领时记录）',
  `quantity` INT NOT NULL COMMENT '消耗数量',
  `type` VARCHAR(20) NOT NULL COMMENT '类型: APPLICATION-申领, OUTBOUND-出库',
  `record_date` DATE NOT NULL COMMENT '记录日期',
  `record_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  `application_id` BIGINT COMMENT '关联申领单ID',
  `stock_out_id` BIGINT COMMENT '关联出库单ID',
  PRIMARY KEY (`id`),
  KEY `idx_material_id` (`material_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_record_date` (`record_date`),
  CONSTRAINT `fk_consumption_material` FOREIGN KEY (`material_id`) REFERENCES `material` (`id`),
  CONSTRAINT `fk_consumption_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='历史消耗记录表';

-- 预警记录表
CREATE TABLE `alert_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预警ID',
  `material_id` BIGINT NOT NULL COMMENT '物资ID',
  `alert_type` VARCHAR(50) NOT NULL COMMENT '预警类型: LOW_STOCK-低库存, ABNORMAL_APPLICATION-异常申领',
  `alert_level` VARCHAR(20) NOT NULL COMMENT '预警等级: HIGH-高, MEDIUM-中, LOW-低',
  `current_value` DECIMAL(15,2) COMMENT '当前值',
  `threshold` DECIMAL(15,2) COMMENT '阈值',
  `message` VARCHAR(500) NOT NULL COMMENT '预警信息',
  `status` VARCHAR(20) NOT NULL DEFAULT 'UNREAD' COMMENT '状态: UNREAD-未读, READ-已读, PROCESSED-已处理',
  `alert_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预警时间',
  `read_time` DATETIME COMMENT '读取时间',
  `process_time` DATETIME COMMENT '处理时间',
  `process_user_id` BIGINT COMMENT '处理人ID',
  PRIMARY KEY (`id`),
  KEY `idx_material_id` (`material_id`),
  KEY `idx_alert_type` (`alert_type`),
  KEY `idx_status` (`status`),
  KEY `idx_alert_time` (`alert_time`),
  CONSTRAINT `fk_alert_material` FOREIGN KEY (`material_id`) REFERENCES `material` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警记录表';

-- 数据报表快照表（可选，用于存储计算好的报表数据）
CREATE TABLE `report_snapshot` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '快照ID',
  `report_type` VARCHAR(50) NOT NULL COMMENT '报表类型',
  `report_date` DATE NOT NULL COMMENT '报表日期',
  `data` JSON NOT NULL COMMENT '报表数据(JSON格式)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_date` (`report_type`, `report_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据报表快照表';

-- 初始化管理员用户（密码：admin123）
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`, `email`, `phone`, `department`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iK6roS6D4YVqO09tJ6R7DQc6OFG2', '系统管理员', 'ADMIN', 1, 'admin@example.com', '13800138000', 'IT部');

-- 初始化普通用户（密码：user123）
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`, `email`, `phone`, `department`) VALUES
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iK6roS6D4YVqO09tJ6R7DQc6OFG2', '张三', 'USER', 1, 'zhangsan@example.com', '13800138001', '行政部'),
('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iK6roS6D4YVqO09tJ6R7DQc6OFG2', '李四', 'USER', 1, 'lisi@example.com', '13800138002', '财务部');

-- 初始化示例物资数据
INSERT INTO `material` (`code`, `name`, `category`, `specification`, `unit`, `unit_price`, `lead_time`, `safety_factor`, `current_stock`, `min_stock`, `max_stock`) VALUES
('MS001', 'A4打印纸', '办公用品', '80g,500张/包', '包', 25.00, 7, 1.5, 100, 20, 200),
('MS002', '黑色签字笔', '办公用品', '0.5mm,12支/盒', '盒', 12.00, 5, 1.2, 200, 50, 300),
('MS003', '办公椅', '办公家具', '人体工学椅', '把', 500.00, 14, 2.0, 10, 2, 20),
('MS004', '订书机', '办公用品', '中型,含订书针', '个', 15.00, 3, 1.0, 30, 5, 50),
('MS005', '笔记本', '办公用品', 'A5,100页', '本', 8.00, 5, 1.5, 150, 30, 200);

-- 创建索引以提高查询性能
CREATE INDEX idx_application_material_user ON application(material_id, user_id);
CREATE INDEX idx_consumption_material_date ON consumption_history(material_id, record_date);
CREATE INDEX idx_stock_in_material_time ON stock_in(material_id, in_time);
CREATE INDEX idx_stock_out_material_time ON stock_out(material_id, out_time);

-- 创建视图：物资库存视图
CREATE VIEW material_inventory_view AS
SELECT
  m.id,
  m.code,
  m.name,
  m.category,
  m.specification,
  m.unit,
  m.unit_price,
  m.current_stock,
  m.min_stock,
  m.max_stock,
  m.abc_class,
  m.annual_consumption,
  m.annual_consumption_amount,
  CASE
    WHEN m.current_stock <= m.min_stock THEN 'LOW'
    WHEN m.current_stock >= m.max_stock THEN 'HIGH'
    ELSE 'NORMAL'
  END as stock_status
FROM material m;

-- 创建视图：申领单详情视图
CREATE VIEW application_detail_view AS
SELECT
  a.id,
  a.application_no,
  a.user_id,
  u.real_name as user_name,
  u.department as user_department,
  a.material_id,
  m.name as material_name,
  m.code as material_code,
  m.unit as material_unit,
  a.quantity,
  a.purpose,
  a.status,
  a.apply_time,
  a.approve_time,
  a.approve_user_id,
  au.real_name as approve_user_name,
  a.reject_reason,
  a.outbound_time,
  a.outbound_user_id,
  ou.real_name as outbound_user_name,
  a.is_abnormal,
  a.abnormal_reason,
  a.remark
FROM application a
LEFT JOIN user u ON a.user_id = u.id
LEFT JOIN user au ON a.approve_user_id = au.id
LEFT JOIN user ou ON a.outbound_user_id = ou.id
LEFT JOIN material m ON a.material_id = m.id;