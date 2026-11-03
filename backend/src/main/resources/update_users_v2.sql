-- 更新用户表数据和角色
-- 首先禁用外键约束检查
SET FOREIGN_KEY_CHECKS = 0;

-- 删除之前可能插入的admin用户（避免重复）
DELETE FROM user WHERE username = 'admin';

-- 更新现有用户的角色为employee
UPDATE user SET role = 'employee' WHERE role = 'USER';

-- 插入管理员用户（如果不存在）
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`, `email`, `phone`, `department`)
SELECT 'admin', '0192023a7bbd73250516f069df18b500', 'Admin', 'admin', 1, 'admin@example.com', '13800138000', 'IT'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'admin');

-- 插入更多员工用户，总共达到10条记录（包括现有用户）
-- 首先计算现有员工数量
SET @employee_count = (SELECT COUNT(*) FROM user WHERE role = 'employee');

-- 需要插入的员工数量
SET @needed = 10 - (SELECT COUNT(*) FROM user);

-- 插入新员工（使用简单的用户名和密码）
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`, `email`, `phone`, `department`) VALUES
('emp1', '6ad14ba9986e3615423dfca256d04e3f', 'Employee1', 'employee', 1, 'emp1@example.com', '13800138011', 'Admin'),
('emp2', 'e10adc3949ba59abbe56e057f20f883e', 'Employee2', 'employee', 1, 'emp2@example.com', '13800138012', 'Finance'),
('emp3', '6ad14ba9986e3615423dfca256d04e3f', 'Employee3', 'employee', 1, 'emp3@example.com', '13800138013', 'HR'),
('emp4', 'e10adc3949ba59abbe56e057f20f883e', 'Employee4', 'employee', 1, 'emp4@example.com', '13800138014', 'Procurement'),
('emp5', '6ad14ba9986e3615423dfca256d04e3f', 'Employee5', 'employee', 1, 'emp5@example.com', '13800138015', 'Sales'),
('emp6', 'e10adc3949ba59abbe56e057f20f883e', 'Employee6', 'employee', 1, 'emp6@example.com', '13800138016', 'Tech'),
('emp7', '6ad14ba9986e3615423dfca256d04e3f', 'Employee7', 'employee', 1, 'emp7@example.com', '13800138017', 'Marketing'),
('emp8', 'e10adc3949ba59abbe56e057f20f883e', 'Employee8', 'employee', 1, 'emp8@example.com', '13800138018', 'Operations'),
('emp9', '6ad14ba9986e3615423dfca256d04e3f', 'Employee9', 'employee', 1, 'emp9@example.com', '13800138019', 'CustomerService');

-- 启用外键约束检查
SET FOREIGN_KEY_CHECKS = 1;

-- 验证插入的数据
SELECT id, username, role, status FROM user ORDER BY id;