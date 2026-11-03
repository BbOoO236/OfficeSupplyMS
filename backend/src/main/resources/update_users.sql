-- 更新用户表数据
-- 首先禁用外键约束检查
SET FOREIGN_KEY_CHECKS = 0;

-- 删除所有现有用户数据
DELETE FROM user;
ALTER TABLE user AUTO_INCREMENT = 1;

-- 插入10条用户数据，包括管理员和员工
-- 密码使用MD5哈希，登录后会自动迁移到BCrypt
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`, `email`, `phone`, `department`) VALUES
-- 管理员账户 (密码: admin123)
('admin', '0192023a7bbd73250516f069df18b500', '管理员', 'admin', 1, 'admin@example.com', '13800138000', 'IT部'),
-- 员工账户 (密码: user123)
('zhangsan', '6ad14ba9986e3615423dfca256d04e3f', '张三', 'employee', 1, 'zhangsan@example.com', '13800138001', '行政部'),
('lisi', '6ad14ba9986e3615423dfca256d04e3f', '李四', 'employee', 1, 'lisi@example.com', '13800138002', '财务部'),
('wangwu', 'e10adc3949ba59abbe56e057f20f883e', '王五', 'employee', 1, 'wangwu@example.com', '13800138003', '人力资源部'),
('zhaoliu', 'e10adc3949ba59abbe56e057f20f883e', '赵六', 'employee', 1, 'zhaoliu@example.com', '13800138004', '采购部'),
('sunqi', '6ad14ba9986e3615423dfca256d04e3f', '孙七', 'employee', 1, 'sunqi@example.com', '13800138005', '销售部'),
('zhouba', 'e10adc3949ba59abbe56e057f20f883e', '周八', 'employee', 1, 'zhouba@example.com', '13800138006', '技术部'),
('wujiu', '6ad14ba9986e3615423dfca256d04e3f', '吴九', 'employee', 1, 'wujiu@example.com', '13800138007', '市场部'),
('zhengshi', 'e10adc3949ba59abbe56e057f20f883e', '郑十', 'employee', 1, 'zhengshi@example.com', '13800138008', '运营部'),
('liuyi', '6ad14ba9986e3615423dfca256d04e3f', '刘一', 'employee', 1, 'liuyi@example.com', '13800138009', '客服部');

-- 启用外键约束检查
SET FOREIGN_KEY_CHECKS = 1;

-- 验证插入的数据
SELECT id, username, role, status FROM user;