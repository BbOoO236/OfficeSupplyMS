package com.officesupplyms.service;

import com.officesupplyms.model.entity.Application;
import com.officesupplyms.model.entity.Material;

import java.util.List;
import java.util.Map;

/**
 * 仪表盘服务接口
 */
public interface DashboardService {

    /**
     * 获取管理员仪表盘数据
     */
    Map<String, Object> getAdminDashboard();

    /**
     * 获取员工仪表盘数据
     */
    Map<String, Object> getEmployeeDashboard();

    /**
     * 获取待审批申请列表
     */
    List<Application> getPendingApplications();

    /**
     * 获取待出库申请列表
     */
    List<Application> getToBeOutboundApplications();

    /**
     * 获取低库存预警物资列表
     */
    List<Material> getLowStockWarnings();
}
