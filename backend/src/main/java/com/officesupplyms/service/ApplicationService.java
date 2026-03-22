package com.officesupplyms.service;

import com.officesupplyms.model.dto.ApplicationDTO;
import com.officesupplyms.model.dto.ApproveDTO;
import com.officesupplyms.model.vo.ApplicationVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 申领服务接口
 */
public interface ApplicationService {

    /**
     * 创建申领单
     */
    ApplicationVO createApplication(ApplicationDTO applicationDTO);

    /**
     * 审批申领单
     */
    ApplicationVO approveApplication(ApproveDTO approveDTO);

    /**
     * 出库操作
     */
    ApplicationVO outboundApplication(Long applicationId);

    /**
     * 根据ID查询申领单
     */
    ApplicationVO getApplicationById(Long applicationId);

    /**
     * 根据申领单号查询
     */
    ApplicationVO getApplicationByNo(String applicationNo);

    /**
     * 查询当前用户的申领记录
     */
    List<ApplicationVO> getMyApplications();

    /**
     * 查询所有申领记录（管理员）
     */
    List<ApplicationVO> getAllApplications();

    /**
     * 根据状态查询申领记录
     */
    List<ApplicationVO> getApplicationsByStatus(String status);

    /**
     * 根据时间范围查询申领记录
     */
    List<ApplicationVO> getApplicationsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取待审批申领单列表
     */
    List<ApplicationVO> getPendingApplications();

    /**
     * 获取待出库申领单列表
     */
    List<ApplicationVO> getToBeOutboundApplications();

    /**
     * 获取异常申领记录
     */
    List<ApplicationVO> getAbnormalApplications();

    /**
     * 统计各物资申领数量TOP10
     */
    List<Map<String, Object>> getTop10MaterialsByApplication();

    /**
     * 统计每月申领单数量与金额
     */
    List<Map<String, Object>> getMonthlyApplicationStats();

    /**
     * 检测申领是否异常（3σ原则）
     */
    boolean detectAnomaly(Long userId, Long materialId, Integer quantity);
}