package com.officesupplyms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.officesupplyms.mapper.ApplicationMapper;
import com.officesupplyms.mapper.MaterialMapper;
import com.officesupplyms.mapper.UserMapper;
import com.officesupplyms.model.entity.Application;
import com.officesupplyms.model.entity.Material;
import com.officesupplyms.model.entity.User;
import com.officesupplyms.service.DashboardService;
import com.officesupplyms.service.MaterialService;
import com.officesupplyms.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 仪表盘服务实现类
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Map<String, Object> getAdminDashboard() {
        Map<String, Object> result = new HashMap<>();

        // 统计待审批数量
        List<Application> pendingList = getPendingApplications();

        // 统计今日申请数量
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().plusDays(1).atStartOfDay();
        QueryWrapper<Application> todayQuery = new QueryWrapper<>();
        todayQuery.ge("apply_time", todayStart);
        todayQuery.lt("apply_time", todayEnd);
        long todayApplicationCount = applicationMapper.selectCount(todayQuery);

        // 统计本月消耗数量
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        QueryWrapper<Application> monthQuery = new QueryWrapper<>();
        monthQuery.ge("apply_time", monthStart);
        monthQuery.eq("status", "OUTBOUND");
        List<Application> monthlyOutbound = applicationMapper.selectList(monthQuery);
        int monthlyUsageCount = monthlyOutbound.stream().mapToInt(Application::getQuantity).sum();

        // 统计已完成数量（所有已出库的审批单）
        QueryWrapper<Application> outboundQuery = new QueryWrapper<>();
        outboundQuery.eq("status", "OUTBOUND");
        long allOutboundCount = applicationMapper.selectCount(outboundQuery);

        Map<String, Object> stats = new HashMap<>();
        stats.put("todayApplications", todayApplicationCount);
        stats.put("pendingApproval", pendingList.size());
        stats.put("monthlyUsage", monthlyUsageCount);
        stats.put("needReorderCount", getNeedReorderCount());
        stats.put("completedApplications", allOutboundCount);

        result.put("stats", stats);

        // 待审批列表 - 转换为包含物资名称的格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<Map<String, Object>> pendingApprovalList = pendingList.stream().map(app -> {
            Map<String, Object> map = new HashMap<>();
            map.put("applicationNo", app.getApplicationNo() != null ? app.getApplicationNo() : "");
            Material material = materialMapper.selectById(app.getMaterialId());
            map.put("materialName", material != null ? material.getName() : "");
            map.put("quantity", app.getQuantity() != null ? app.getQuantity() : 0);
            map.put("applicationTime", app.getApplyTime() != null ? app.getApplyTime().format(formatter) : "");
            map.put("status", app.getStatus() != null ? app.getStatus() : "");
            return map;
        }).collect(Collectors.toList());
        result.put("pendingApprovalList", pendingApprovalList);

        // 待出库列表 - 转换为包含物资名称的格式
        List<Application> toBeOutboundList = getToBeOutboundApplications();
        List<Map<String, Object>> pendingOutboundList = toBeOutboundList.stream().map(app -> {
            Map<String, Object> map = new HashMap<>();
            map.put("applicationNo", app.getApplicationNo() != null ? app.getApplicationNo() : "");
            Material material = materialMapper.selectById(app.getMaterialId());
            map.put("materialName", material != null ? material.getName() : "");
            map.put("quantity", app.getQuantity() != null ? app.getQuantity() : 0);
            map.put("applicationTime", app.getApplyTime() != null ? app.getApplyTime().format(formatter) : "");
            map.put("status", app.getStatus() != null ? app.getStatus() : "");
            return map;
        }).collect(Collectors.toList());
        result.put("pendingOutboundList", pendingOutboundList);

        // 库存预警列表
        result.put("warnings", getLowStockWarnings());

        // 消耗趋势 (最近6个月的消耗数据)
        result.put("consumptionTrend", getConsumptionTrend());

        return result;
    }

    @Override
    public Map<String, Object> getEmployeeDashboard() {
        Map<String, Object> result = new HashMap<>();

        Long currentUserId = getCurrentUserId();

        // 获取用户的申领记录
        List<Application> userApplications = applicationMapper.selectByUserId(currentUserId);

        // 统计今日申请数量
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().plusDays(1).atStartOfDay();
        long todayApplicationCount = userApplications.stream()
                .filter(a -> a.getApplyTime() != null && a.getApplyTime().isAfter(todayStart) && a.getApplyTime().isBefore(todayEnd))
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("todayApplications", todayApplicationCount);

        // 统计待审批数量
        long pendingCount = userApplications.stream()
                .filter(a -> "PENDING".equals(a.getStatus()))
                .count();
        stats.put("pendingApproval", pendingCount);

        result.put("stats", stats);

        // 最近申领记录 (只取最近5条)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<Map<String, Object>> recentApplications = userApplications.stream()
                .limit(5)
                .map(app -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("applicationNo", app.getApplicationNo() != null ? app.getApplicationNo() : "");
                    Material material = materialMapper.selectById(app.getMaterialId());
                    map.put("materialName", material != null ? material.getName() : "");
                    map.put("quantity", app.getQuantity() != null ? app.getQuantity() : 0);
                    map.put("applicationTime", app.getApplyTime() != null ? app.getApplyTime().format(formatter) : "");
                    map.put("status", app.getStatus() != null ? app.getStatus() : "");
                    return map;
                })
                .collect(Collectors.toList());

        result.put("recentApplications", recentApplications);

        return result;
    }

    @Override
    public List<Application> getPendingApplications() {
        return applicationMapper.selectByStatus("PENDING");
    }

    @Override
    public List<Application> getToBeOutboundApplications() {
        return applicationMapper.selectByStatus("TO_BE_OUTBOUND");
    }

    @Override
    public List<Material> getLowStockWarnings() {
        QueryWrapper<Material> queryWrapper = new QueryWrapper<>();
        queryWrapper.apply("current_stock < min_stock");
        queryWrapper.eq("status", 1);
        return materialMapper.selectList(queryWrapper);
    }

    /**
     * 获取需补货物资数量（使用与库存管理一致的算法计算）
     */
    private int getNeedReorderCount() {
        return materialService.getNeedReorderMaterials().size();
    }

    /**
     * 获取最近6个月的消耗趋势
     */
    private List<Map<String, Object>> getConsumptionTrend() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);

        // 使用QueryWrapper替代selectByTimeRange
        QueryWrapper<Application> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("apply_time", sixMonthsAgo);
        queryWrapper.eq("status", "COMPLETED");
        List<Application> applications = applicationMapper.selectList(queryWrapper);

        // 按月份分组统计
        Map<String, Integer> monthlyConsumption = applications.stream()
                .collect(Collectors.groupingBy(
                        app -> app.getApplyTime().getYear() + "-" + String.format("%02d", app.getApplyTime().getMonthValue()),
                        Collectors.summingInt(Application::getQuantity)
                ));

        return monthlyConsumption.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("month", entry.getKey());
                    map.put("quantity", entry.getValue());
                    return map;
                })
                .sorted((a, b) -> a.get("month").toString().compareTo(b.get("month").toString()))
                .collect(Collectors.toList());
    }

    private Long getCurrentUserId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return 1L; // 默认返回管理员ID
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return 1L;
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}
