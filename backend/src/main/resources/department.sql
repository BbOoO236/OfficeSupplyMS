-- 部门管理表
CREATE TABLE IF NOT EXISTS `department` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '部门ID',
  `name` VARCHAR(100) NOT NULL COMMENT '部门名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '部门描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 初始化部门数据
INSERT INTO `department` (`name`, `description`, `status`) VALUES
('IT', '信息技术部', 1),
('Admin', '综合管理部', 1),
('Finance', '财务部', 1),
('HR', '人力资源部', 1),
('Procurement', '采购部', 1),
('Sales', '销售部', 1),
('Tech', '技术部', 1),
('Marketing', '市场部', 1),
('Operations', '运营部', 1),
('CustomerService', '客服部', 1);
