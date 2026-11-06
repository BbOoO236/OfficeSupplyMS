package com.officesupplyms.controller;

import com.officesupplyms.service.DashboardService;
import com.officesupplyms.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 仪表盘控制器
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取管理员仪表盘数据
     */
    @GetMapping("/admin")
    public Result<Map<String, Object>> getAdminDashboard() {
        return Result.success(dashboardService.getAdminDashboard());
    }

    /**
     * 获取员工仪表盘数据
     */
    @GetMapping("/employee")
    public Result<Map<String, Object>> getEmployeeDashboard() {
        return Result.success(dashboardService.getEmployeeDashboard());
    }
}
