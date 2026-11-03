-- Test data for algorithm testing
-- Run this AFTER database.sql
-- Encoding: UTF-8

USE office_supply_ms;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. Check existing data first
-- ============================================
-- Existing users: id=1 (admin), id=2 (user1 - Zhang San), id=3 (user2 - Li Si)
-- Existing materials: id=1 to id=5 (from database.sql)

-- ============================================
-- 2. Add more users for anomaly detection by department
-- ============================================

INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`, `email`, `phone`, `department`) VALUES
('user3', '6ad14ba9986e3615423dfca256d04e3f', 'Wang Wu', 'employee', 1, 'wangwu@test.com', '13800138003', 'RD'),
('user4', '6ad14ba9986e3615423dfca256d04e3f', 'Zhao Liu', 'employee', 1, 'zhaoliu@test.com', '13800138004', 'RD'),
('user5', '6ad14ba9986e3615423dfca256d04e3f', 'Qian Qi', 'employee', 1, 'qianqi@test.com', '13800138005', 'Sales'),
('user6', '6ad14ba9986e3615423dfca256d04e3f', 'Sun Ba', 'employee', 1, 'sunba@test.com', '13800138006', 'Sales');

-- After this: users id=1,2,3,4,5,6

-- ============================================
-- 3. Add more materials for ABC classification
-- ============================================

INSERT INTO `material` (`code`, `name`, `category`, `specification`, `unit`, `unit_price`, `lead_time`, `safety_factor`, `current_stock`, `min_stock`, `max_stock`, `status`) VALUES
('MS006', 'Desktop PC', 'Electronics', 'Intel i5/8GB/256GB', 'PC', 4500.00, 15, 1.50, 5, 2, 15, 1),
('MS007', 'Wireless Mouse', 'Electronics', '2.4G Wireless', 'PC', 45.00, 5, 1.20, 80, 20, 150, 1),
('MS008', 'Keyboard', 'Electronics', 'Mechanical', 'PC', 150.00, 7, 1.20, 40, 10, 80, 1),
('MS009', 'Monitor', 'Electronics', '24inch IPS', 'PC', 1200.00, 10, 1.50, 8, 3, 20, 1),
('MS010', 'Headphones', 'Electronics', 'Noise Cancelling', 'PC', 280.00, 7, 1.30, 25, 5, 50, 1),
('MS011', 'External HDD', 'Electronics', '1TB USB3.0', 'PC', 320.00, 5, 1.20, 15, 5, 40, 1),
('MS012', 'USB Flash', 'Electronics', '64GB', 'PC', 35.00, 3, 1.10, 60, 15, 120, 1),
('MS013', 'Printer Toner', 'Supplies', 'Black Laser', 'PC', 180.00, 5, 1.50, 12, 4, 30, 1),
('MS014', 'Projector', 'Electronics', '1080P Business', 'PC', 3500.00, 20, 2.00, 2, 1, 5, 1),
('MS015', 'Shredder', 'Equipment', 'Large Office', 'PC', 800.00, 10, 1.80, 3, 1, 8, 1),
('MS016', 'Copier', 'Equipment', 'B&W Laser All-in-One', 'PC', 6000.00, 25, 2.00, 1, 1, 4, 1),
('MS017', 'File Cabinet', 'Furniture', '4-Door Steel', 'PC', 650.00, 14, 1.50, 4, 2, 10, 1),
('MS018', 'Whiteboard', 'Supplies', '120x90cm', 'PC', 120.00, 7, 1.20, 8, 2, 20, 1),
('MS019', 'Marker Set', 'Supplies', '12 Colors', 'PC', 22.00, 5, 1.00, 45, 10, 100, 1),
('MS020', 'Paper Clip', 'Supplies', '100pcs/box', 'PC', 3.00, 3, 1.00, 200, 50, 500, 1);

-- After this: materials id=1 to 20

-- ============================================
-- 4. Add stock in records (establish inventory)
-- ============================================

INSERT INTO `stock_in` (`stock_in_no`, `material_id`, `quantity`, `unit_price`, `total_amount`, `supplier`, `in_time`, `operator_id`, `remark`) VALUES
('IN20250115001', 1, 100, 25.00, 2500.00, 'Supplier A', '2025-01-15 09:00:00', 1, 'Office supplies'),
('IN20250118002', 2, 200, 12.00, 2400.00, 'Supplier B', '2025-01-18 10:30:00', 1, 'Stationery'),
('IN20250120003', 3, 10, 500.00, 5000.00, 'Supplier C', '2025-01-20 14:00:00', 1, 'Furniture'),
('IN20250210004', 4, 50, 15.00, 750.00, 'Supplier A', '2025-02-10 11:00:00', 1, 'Office supplies'),
('IN20250215005', 5, 150, 8.00, 1200.00, 'Supplier B', '2025-02-15 09:30:00', 1, 'Stationery'),
('IN20250301006', 6, 20, 4500.00, 90000.00, 'Supplier D', '2025-03-01 10:00:00', 1, 'PC purchase'),
('IN20250305007', 7, 100, 45.00, 4500.00, 'Supplier E', '2025-03-05 15:00:00', 1, 'Mouse purchase'),
('IN20250310008', 8, 50, 150.00, 7500.00, 'Supplier F', '2025-03-10 14:00:00', 1, 'Keyboard purchase'),
('IN20250315009', 9, 10, 1200.00, 12000.00, 'Supplier G', '2025-03-15 10:00:00', 1, 'Monitor purchase'),
('IN20250320010', 10, 30, 280.00, 8400.00, 'Supplier H', '2025-03-20 11:00:00', 1, 'Headphones purchase'),
('IN20250401011', 11, 20, 320.00, 6400.00, 'Supplier I', '2025-04-01 09:00:00', 1, 'HDD purchase'),
('IN20250405012', 12, 80, 35.00, 2800.00, 'Supplier J', '2025-04-05 10:00:00', 1, 'USB purchase'),
('IN20250410013', 13, 20, 180.00, 3600.00, 'Supplier K', '2025-04-10 14:00:00', 1, 'Toner purchase'),
('IN20250415014', 14, 3, 3500.00, 10500.00, 'Supplier L', '2025-04-15 10:00:00', 1, 'Projector purchase'),
('IN20250420015', 15, 5, 800.00, 4000.00, 'Supplier M', '2025-04-20 11:00:00', 1, 'Shredder purchase'),
('IN20250501016', 16, 2, 6000.00, 12000.00, 'Supplier N', '2025-05-01 09:00:00', 1, 'Copier purchase'),
('IN20250505017', 17, 6, 650.00, 3900.00, 'Supplier C', '2025-05-05 14:00:00', 1, 'Cabinet purchase'),
('IN20250510018', 18, 10, 120.00, 1200.00, 'Supplier A', '2025-05-10 10:00:00', 1, 'Whiteboard purchase'),
('IN20250515019', 19, 60, 22.00, 1320.00, 'Supplier B', '2025-05-15 11:00:00', 1, 'Marker purchase'),
('IN20250520020', 20, 300, 3.00, 900.00, 'Supplier A', '2025-05-20 09:00:00', 1, 'Clip purchase');

-- ============================================
-- 5. Add historical applications (2024-2026)
-- ============================================

INSERT INTO `application` (`application_no`, `user_id`, `material_id`, `quantity`, `purpose`, `status`, `apply_time`, `approve_time`, `approve_user_id`, `outbound_time`, `outbound_user_id`, `is_abnormal`) VALUES
-- 2024 data (use user_id 2,3,4,5 which will be ZhangSan, LiSi, WangWu, ZhaoLiu)
('APP20240115001', 2, 1, 10, 'Daily office use', 'COMPLETED', '2024-01-15 09:00:00', '2024-01-15 10:00:00', 1, '2024-01-15 14:00:00', 1, 0),
('APP20240118002', 3, 2, 5, 'Department stationery', 'COMPLETED', '2024-01-18 10:00:00', '2024-01-18 11:00:00', 1, '2024-01-18 15:00:00', 1, 0),
('APP20240120003', 2, 5, 20, 'Meeting notes', 'COMPLETED', '2024-01-20 08:00:00', '2024-01-20 09:00:00', 1, '2024-01-20 16:00:00', 1, 0),
('APP20240122004', 4, 1, 8, 'Daily office', 'COMPLETED', '2024-01-22 09:00:00', '2024-01-22 10:00:00', 1, '2024-01-22 14:00:00', 1, 0),
('APP20240125005', 5, 2, 12, 'Department stationery', 'COMPLETED', '2024-01-25 10:00:00', '2024-01-25 11:00:00', 1, '2024-01-25 15:00:00', 1, 0),
-- 2024 Feb
('APP20240210006', 3, 1, 15, 'Office supplies', 'COMPLETED', '2024-02-10 09:00:00', '2024-02-10 10:00:00', 1, '2024-02-10 14:00:00', 1, 0),
('APP20240215007', 4, 7, 3, 'RD equipment', 'COMPLETED', '2024-02-15 10:00:00', '2024-02-15 11:00:00', 1, '2024-02-15 15:00:00', 1, 0),
('APP20240218008', 2, 5, 25, 'Department use', 'COMPLETED', '2024-02-18 08:00:00', '2024-02-18 09:00:00', 1, '2024-02-18 16:00:00', 1, 0),
('APP20240220009', 5, 8, 2, 'Keyboard purchase', 'COMPLETED', '2024-02-20 09:00:00', '2024-02-20 10:00:00', 1, '2024-02-20 14:00:00', 1, 0),
('APP20240225010', 3, 2, 8, 'Stationery request', 'COMPLETED', '2024-02-25 10:00:00', '2024-02-25 11:00:00', 1, '2024-02-25 15:00:00', 1, 0),
-- 2024 Mar
('APP20240305011', 2, 1, 12, 'Daily office', 'COMPLETED', '2024-03-05 09:00:00', '2024-03-05 10:00:00', 1, '2024-03-05 14:00:00', 1, 0),
('APP20240310012', 4, 13, 4, 'Print supplies', 'COMPLETED', '2024-03-10 10:00:00', '2024-03-10 11:00:00', 1, '2024-03-10 15:00:00', 1, 0),
('APP20240315013', 3, 5, 18, 'Department use', 'COMPLETED', '2024-03-15 08:00:00', '2024-03-15 09:00:00', 1, '2024-03-15 16:00:00', 1, 0),
('APP20240320014', 5, 7, 5, 'RD supplies', 'COMPLETED', '2024-03-20 09:00:00', '2024-03-20 10:00:00', 1, '2024-03-20 14:00:00', 1, 0),
('APP20240325015', 2, 2, 10, 'Stationery request', 'COMPLETED', '2024-03-25 10:00:00', '2024-03-25 11:00:00', 1, '2024-03-25 15:00:00', 1, 0),
-- 2024 Apr
('APP20240405016', 4, 1, 14, 'Office supplies', 'COMPLETED', '2024-04-05 09:00:00', '2024-04-05 10:00:00', 1, '2024-04-05 14:00:00', 1, 0),
('APP20240410017', 3, 8, 3, 'Keyboard replacement', 'COMPLETED', '2024-04-10 10:00:00', '2024-04-10 11:00:00', 1, '2024-04-10 15:00:00', 1, 0),
('APP20240415018', 2, 5, 22, 'Department purchase', 'COMPLETED', '2024-04-15 08:00:00', '2024-04-15 09:00:00', 1, '2024-04-15 16:00:00', 1, 0),
('APP20240420019', 5, 13, 6, 'Print supplies', 'COMPLETED', '2024-04-20 09:00:00', '2024-04-20 10:00:00', 1, '2024-04-20 14:00:00', 1, 0),
('APP20240425020', 3, 2, 9, 'Stationery request', 'COMPLETED', '2024-04-25 10:00:00', '2024-04-25 11:00:00', 1, '2024-04-25 15:00:00', 1, 0),
-- 2024 May
('APP20240505021', 2, 1, 11, 'Daily office', 'COMPLETED', '2024-05-05 09:00:00', '2024-05-05 10:00:00', 1, '2024-05-05 14:00:00', 1, 0),
('APP20240510022', 4, 7, 4, 'Mouse purchase', 'COMPLETED', '2024-05-10 10:00:00', '2024-05-10 11:00:00', 1, '2024-05-10 15:00:00', 1, 0),
('APP20240515023', 3, 5, 16, 'Department use', 'COMPLETED', '2024-05-15 08:00:00', '2024-05-15 09:00:00', 1, '2024-05-15 16:00:00', 1, 0),
('APP20240520024', 5, 8, 2, 'Keyboard request', 'COMPLETED', '2024-05-20 09:00:00', '2024-05-20 10:00:00', 1, '2024-05-20 14:00:00', 1, 0),
('APP20240525025', 4, 2, 7, 'Stationery purchase', 'COMPLETED', '2024-05-25 10:00:00', '2024-05-25 11:00:00', 1, '2024-05-25 15:00:00', 1, 0),
-- 2024 Jun
('APP20240605026', 2, 1, 13, 'Office supplies', 'COMPLETED', '2024-06-05 09:00:00', '2024-06-05 10:00:00', 1, '2024-06-05 14:00:00', 1, 0),
('APP20240610027', 3, 13, 5, 'Print supplies', 'COMPLETED', '2024-06-10 10:00:00', '2024-06-10 11:00:00', 1, '2024-06-10 15:00:00', 1, 0),
('APP20240615028', 4, 5, 19, 'Department use', 'COMPLETED', '2024-06-15 08:00:00', '2024-06-15 09:00:00', 1, '2024-06-15 16:00:00', 1, 0),
('APP20240620029', 5, 7, 6, 'RD equipment', 'COMPLETED', '2024-06-20 09:00:00', '2024-06-20 10:00:00', 1, '2024-06-20 14:00:00', 1, 0),
('APP20240625030', 2, 2, 11, 'Stationery request', 'COMPLETED', '2024-06-25 10:00:00', '2024-06-25 11:00:00', 1, '2024-06-25 15:00:00', 1, 0),
-- 2024 second half
('APP20240705031', 3, 1, 10, 'Office supplies', 'COMPLETED', '2024-07-05 09:00:00', '2024-07-05 10:00:00', 1, '2024-07-05 14:00:00', 1, 0),
('APP20240810032', 4, 5, 20, 'Department purchase', 'COMPLETED', '2024-08-10 10:00:00', '2024-08-10 11:00:00', 1, '2024-08-10 15:00:00', 1, 0),
('APP20240915033', 2, 13, 4, 'Supplies request', 'COMPLETED', '2024-09-15 08:00:00', '2024-09-15 09:00:00', 1, '2024-09-15 16:00:00', 1, 0),
('APP20241010034', 5, 7, 5, 'Mouse purchase', 'COMPLETED', '2024-10-10 09:00:00', '2024-10-10 10:00:00', 1, '2024-10-10 14:00:00', 1, 0),
('APP20241115035', 3, 2, 8, 'Stationery purchase', 'COMPLETED', '2024-11-15 10:00:00', '2024-11-15 11:00:00', 1, '2024-11-15 15:00:00', 1, 0),
('APP20241210036', 4, 1, 12, 'Office supplies', 'COMPLETED', '2024-12-10 09:00:00', '2024-12-10 10:00:00', 1, '2024-12-10 14:00:00', 1, 0),
-- 2025 data
('APP20250105037', 2, 5, 18, 'Department use', 'COMPLETED', '2025-01-05 09:00:00', '2025-01-05 10:00:00', 1, '2025-01-05 14:00:00', 1, 0),
('APP20250210038', 3, 7, 4, 'RD supplies', 'COMPLETED', '2025-02-10 10:00:00', '2025-02-10 11:00:00', 1, '2025-02-10 15:00:00', 1, 0),
('APP20250315039', 4, 13, 3, 'Print supplies', 'COMPLETED', '2025-03-15 08:00:00', '2025-03-15 09:00:00', 1, '2025-03-15 16:00:00', 1, 0),
('APP20250410040', 5, 8, 2, 'Keyboard replacement', 'COMPLETED', '2025-04-10 09:00:00', '2025-04-10 10:00:00', 1, '2025-04-10 14:00:00', 1, 0),
-- 2026 data
('APP20260105041', 2, 1, 15, 'Office supplies', 'COMPLETED', '2026-01-05 09:00:00', '2026-01-05 10:00:00', 1, '2026-01-05 14:00:00', 1, 0),
('APP20260210042', 3, 5, 22, 'Department use', 'COMPLETED', '2026-02-10 10:00:00', '2026-02-10 11:00:00', 1, '2026-02-10 15:00:00', 1, 0),
('APP20260315043', 4, 2, 9, 'Stationery request', 'COMPLETED', '2026-03-15 08:00:00', '2026-03-15 09:00:00', 1, '2026-03-15 16:00:00', 1, 0);

-- ============================================
-- 6. Add stock out records (applications 1-43)
-- ============================================

INSERT INTO `stock_out` (`stock_out_no`, `material_id`, `quantity`, `type`, `application_id`, `out_time`, `operator_id`, `reason`) VALUES
('OUT20240115001', 1, 10, 'APPLICATION', 1, '2024-01-15 14:00:00', 1, 'Daily office'),
('OUT20240118002', 2, 5, 'APPLICATION', 2, '2024-01-18 15:00:00', 1, 'Stationery'),
('OUT20240120003', 5, 20, 'APPLICATION', 3, '2024-01-20 16:00:00', 1, 'Meeting notes'),
('OUT20240122004', 1, 8, 'APPLICATION', 4, '2024-01-22 14:00:00', 1, 'Daily office'),
('OUT20240125005', 2, 12, 'APPLICATION', 5, '2024-01-25 15:00:00', 1, 'Stationery'),
('OUT20240210006', 1, 15, 'APPLICATION', 6, '2024-02-10 14:00:00', 1, 'Office supplies'),
('OUT20240215007', 7, 3, 'APPLICATION', 7, '2024-02-15 15:00:00', 1, 'RD equipment'),
('OUT20240218008', 5, 25, 'APPLICATION', 8, '2024-02-18 16:00:00', 1, 'Department'),
('OUT20240220009', 8, 2, 'APPLICATION', 9, '2024-02-20 14:00:00', 1, 'Keyboard'),
('OUT20240225010', 2, 8, 'APPLICATION', 10, '2024-02-25 15:00:00', 1, 'Stationery'),
('OUT20240305011', 1, 12, 'APPLICATION', 11, '2024-03-05 14:00:00', 1, 'Daily office'),
('OUT20240310012', 13, 4, 'APPLICATION', 12, '2024-03-10 15:00:00', 1, 'Print'),
('OUT20240315013', 5, 18, 'APPLICATION', 13, '2024-03-15 16:00:00', 1, 'Department'),
('OUT20240320014', 7, 5, 'APPLICATION', 14, '2024-03-20 14:00:00', 1, 'RD'),
('OUT20240325015', 2, 10, 'APPLICATION', 15, '2024-03-25 15:00:00', 1, 'Stationery'),
('OUT20240405016', 1, 14, 'APPLICATION', 16, '2024-04-05 14:00:00', 1, 'Office supplies'),
('OUT20240410017', 8, 3, 'APPLICATION', 17, '2024-04-10 15:00:00', 1, 'Keyboard'),
('OUT20240415018', 5, 22, 'APPLICATION', 18, '2024-04-15 16:00:00', 1, 'Department'),
('OUT20240420019', 13, 6, 'APPLICATION', 19, '2024-04-20 14:00:00', 1, 'Print'),
('OUT20240425020', 2, 9, 'APPLICATION', 20, '2024-04-25 15:00:00', 1, 'Stationery'),
('OUT20240505021', 1, 11, 'APPLICATION', 21, '2024-05-05 14:00:00', 1, 'Daily office'),
('OUT20240510022', 7, 4, 'APPLICATION', 22, '2024-05-10 15:00:00', 1, 'Mouse'),
('OUT20240515023', 5, 16, 'APPLICATION', 23, '2024-05-15 16:00:00', 1, 'Department'),
('OUT20240520024', 8, 2, 'APPLICATION', 24, '2024-05-20 14:00:00', 1, 'Keyboard'),
('OUT20240525025', 2, 7, 'APPLICATION', 25, '2024-05-25 15:00:00', 1, 'Stationery'),
('OUT20240605026', 1, 13, 'APPLICATION', 26, '2024-06-05 14:00:00', 1, 'Office supplies'),
('OUT20240610027', 13, 5, 'APPLICATION', 27, '2024-06-10 15:00:00', 1, 'Print'),
('OUT20240615028', 5, 19, 'APPLICATION', 28, '2024-06-15 16:00:00', 1, 'Department'),
('OUT20240620029', 7, 6, 'APPLICATION', 29, '2024-06-20 14:00:00', 1, 'RD'),
('OUT20240625030', 2, 11, 'APPLICATION', 30, '2024-06-25 15:00:00', 1, 'Stationery'),
('OUT20240705031', 1, 10, 'APPLICATION', 31, '2024-07-05 14:00:00', 1, 'Office supplies'),
('OUT20240810032', 5, 20, 'APPLICATION', 32, '2024-08-10 15:00:00', 1, 'Department'),
('OUT20240915033', 13, 4, 'APPLICATION', 33, '2024-09-15 16:00:00', 1, 'Supplies'),
('OUT20241010034', 7, 5, 'APPLICATION', 34, '2024-10-10 14:00:00', 1, 'Mouse'),
('OUT20241115035', 2, 8, 'APPLICATION', 35, '2024-11-15 15:00:00', 1, 'Stationery'),
('OUT20241210036', 1, 12, 'APPLICATION', 36, '2024-12-10 14:00:00', 1, 'Office supplies'),
('OUT20250105037', 5, 18, 'APPLICATION', 37, '2025-01-05 14:00:00', 1, 'Department'),
('OUT20250210038', 7, 4, 'APPLICATION', 38, '2025-02-10 15:00:00', 1, 'RD'),
('OUT20250315039', 13, 3, 'APPLICATION', 39, '2025-03-15 16:00:00', 1, 'Print'),
('OUT20250410040', 8, 2, 'APPLICATION', 40, '2025-04-10 14:00:00', 1, 'Keyboard'),
('OUT20260105041', 1, 15, 'APPLICATION', 41, '2026-01-05 14:00:00', 1, 'Office supplies'),
('OUT20260210042', 5, 22, 'APPLICATION', 42, '2026-02-10 15:00:00', 1, 'Department'),
('OUT20260315043', 2, 9, 'APPLICATION', 43, '2026-03-15 16:00:00', 1, 'Stationery');

-- ============================================
-- 7. Add consumption history (for demand forecasting)
-- ============================================

INSERT INTO `consumption_history` (`material_id`, `user_id`, `quantity`, `type`, `record_date`, `record_time`, `application_id`, `stock_out_id`) VALUES
-- 2024 data
(1, 2, 10, 'OUTBOUND', '2024-01-15', '2024-01-15 14:00:00', 1, 1),
(2, 3, 5, 'OUTBOUND', '2024-01-18', '2024-01-18 15:00:00', 2, 2),
(5, 2, 20, 'OUTBOUND', '2024-01-20', '2024-01-20 16:00:00', 3, 3),
(1, 4, 8, 'OUTBOUND', '2024-01-22', '2024-01-22 14:00:00', 4, 4),
(2, 5, 12, 'OUTBOUND', '2024-01-25', '2024-01-25 15:00:00', 5, 5),
(1, 2, 15, 'OUTBOUND', '2024-02-10', '2024-02-10 14:00:00', 6, 6),
(7, 3, 3, 'OUTBOUND', '2024-02-15', '2024-02-15 15:00:00', 7, 7),
(5, 2, 25, 'OUTBOUND', '2024-02-18', '2024-02-18 16:00:00', 8, 8),
(8, 4, 2, 'OUTBOUND', '2024-02-20', '2024-02-20 14:00:00', 9, 9),
(2, 3, 8, 'OUTBOUND', '2024-02-25', '2024-02-25 15:00:00', 10, 10),
(1, 2, 12, 'OUTBOUND', '2024-03-05', '2024-03-05 14:00:00', 11, 11),
(13, 4, 4, 'OUTBOUND', '2024-03-10', '2024-03-10 15:00:00', 12, 12),
(5, 3, 18, 'OUTBOUND', '2024-03-15', '2024-03-15 16:00:00', 13, 13),
(7, 4, 5, 'OUTBOUND', '2024-03-20', '2024-03-20 14:00:00', 14, 14),
(2, 2, 10, 'OUTBOUND', '2024-03-25', '2024-03-25 15:00:00', 15, 15),
(1, 4, 14, 'OUTBOUND', '2024-04-05', '2024-04-05 14:00:00', 16, 16),
(8, 3, 3, 'OUTBOUND', '2024-04-10', '2024-04-10 15:00:00', 17, 17),
(5, 2, 22, 'OUTBOUND', '2024-04-15', '2024-04-15 16:00:00', 18, 18),
(13, 5, 6, 'OUTBOUND', '2024-04-20', '2024-04-20 14:00:00', 19, 19),
(2, 3, 9, 'OUTBOUND', '2024-04-25', '2024-04-25 15:00:00', 20, 20),
(1, 2, 11, 'OUTBOUND', '2024-05-05', '2024-05-05 14:00:00', 21, 21),
(7, 4, 4, 'OUTBOUND', '2024-05-10', '2024-05-10 15:00:00', 22, 22),
(5, 3, 16, 'OUTBOUND', '2024-05-15', '2024-05-15 16:00:00', 23, 23),
(8, 5, 2, 'OUTBOUND', '2024-05-20', '2024-05-20 14:00:00', 24, 24),
(2, 4, 7, 'OUTBOUND', '2024-05-25', '2024-05-25 15:00:00', 25, 25),
(1, 2, 13, 'OUTBOUND', '2024-06-05', '2024-06-05 14:00:00', 26, 26),
(13, 3, 5, 'OUTBOUND', '2024-06-10', '2024-06-10 15:00:00', 27, 27),
(5, 4, 19, 'OUTBOUND', '2024-06-15', '2024-06-15 16:00:00', 28, 28),
(7, 5, 6, 'OUTBOUND', '2024-06-20', '2024-06-20 14:00:00', 29, 29),
(2, 2, 11, 'OUTBOUND', '2024-06-25', '2024-06-25 15:00:00', 30, 30),
(1, 3, 10, 'OUTBOUND', '2024-07-05', '2024-07-05 14:00:00', 31, 31),
(5, 4, 20, 'OUTBOUND', '2024-08-10', '2024-08-10 15:00:00', 32, 32),
(13, 2, 4, 'OUTBOUND', '2024-09-15', '2024-09-15 16:00:00', 33, 33),
(7, 5, 5, 'OUTBOUND', '2024-10-10', '2024-10-10 14:00:00', 34, 34),
(2, 3, 8, 'OUTBOUND', '2024-11-15', '2024-11-15 15:00:00', 35, 35),
(1, 4, 12, 'OUTBOUND', '2024-12-10', '2024-12-10 14:00:00', 36, 36),
-- 2025 data
(5, 2, 18, 'OUTBOUND', '2025-01-05', '2025-01-05 14:00:00', 37, 37),
(7, 3, 4, 'OUTBOUND', '2025-02-10', '2025-02-10 15:00:00', 38, 38),
(13, 4, 3, 'OUTBOUND', '2025-03-15', '2025-03-15 16:00:00', 39, 39),
(8, 5, 2, 'OUTBOUND', '2025-04-10', '2025-04-10 14:00:00', 40, 40),
-- 2026 data
(1, 2, 15, 'OUTBOUND', '2026-01-05', '2026-01-05 14:00:00', 41, 41),
(5, 3, 22, 'OUTBOUND', '2026-02-10', '2026-02-10 15:00:00', 42, 42),
(2, 4, 9, 'OUTBOUND', '2026-03-15', '2026-03-15 16:00:00', 43, 43);

-- ============================================
-- 8. Add pending/to-be-outbound applications
-- ============================================

INSERT INTO `application` (`application_no`, `user_id`, `material_id`, `quantity`, `purpose`, `status`, `apply_time`, `is_abnormal`) VALUES
('APP20260401001', 2, 1, 5, 'Daily office use', 'PENDING', '2026-04-01 09:00:00', 0),
('APP20260402002', 3, 2, 3, 'Department stationery', 'PENDING', '2026-04-02 10:00:00', 0),
('APP20260403003', 4, 7, 2, 'RD equipment', 'PENDING', '2026-04-03 11:00:00', 0),
('APP20260404004', 5, 5, 10, 'Meeting notes', 'PENDING', '2026-04-04 08:00:00', 0),
('APP20260405005', 2, 8, 1, 'Keyboard replacement', 'TO_BE_OUTBOUND', '2026-04-05 09:00:00', 0),
('APP20260406006', 3, 13, 2, 'Print supplies', 'TO_BE_OUTBOUND', '2026-04-06 10:00:00', 0);

-- ============================================
-- 9. Add one abnormal application
-- ============================================

INSERT INTO `application` (`application_no`, `user_id`, `material_id`, `quantity`, `purpose`, `status`, `apply_time`, `approve_time`, `approve_user_id`, `is_abnormal`, `abnormal_reason`) VALUES
('APP20260407001', 2, 1, 100, 'Large batch office', 'PENDING', '2026-04-07 09:00:00', '2026-04-07 10:00:00', 1, 1, 'Exceeds 3x historical average');

-- ============================================
-- 10. Update material annual consumption (pre-calculated)
-- ============================================

UPDATE `material` SET `annual_consumption` = 150, `annual_consumption_amount` = 3750.00, `abc_class` = 'A' WHERE `code` = 'MS001';
UPDATE `material` SET `annual_consumption` = 120, `annual_consumption_amount` = 1440.00, `abc_class` = 'B' WHERE `code` = 'MS002';
UPDATE `material` SET `annual_consumption` = 25, `annual_consumption_amount` = 200.00, `abc_class` = 'C' WHERE `code` = 'MS004';
UPDATE `material` SET `annual_consumption` = 200, `annual_consumption_amount` = 1600.00, `abc_class` = 'A' WHERE `code` = 'MS005';
UPDATE `material` SET `annual_consumption` = 30, `annual_consumption_amount` = 13500.00, `abc_class` = 'A' WHERE `code` = 'MS006';
UPDATE `material` SET `annual_consumption` = 50, `annual_consumption_amount` = 2250.00, `abc_class` = 'B' WHERE `code` = 'MS007';
UPDATE `material` SET `annual_consumption` = 20, `annual_consumption_amount` = 3000.00, `abc_class` = 'B' WHERE `code` = 'MS008';
UPDATE `material` SET `annual_consumption` = 35, `annual_consumption_amount` = 540.00, `abc_class` = 'C' WHERE `code` = 'MS013';

SET FOREIGN_KEY_CHECKS = 1;

-- Verify data
SELECT 'Users:' AS '', COUNT(*) AS cnt FROM `user`;
SELECT 'Materials:' AS '', COUNT(*) AS cnt FROM `material`;
SELECT 'Applications:' AS '', COUNT(*) AS cnt FROM `application`;
SELECT 'Stock Outs:' AS '', COUNT(*) AS cnt FROM `stock_out`;
SELECT 'Consumption History:' AS '', COUNT(*) AS cnt FROM `consumption_history`;
SELECT 'Done!' AS '';
