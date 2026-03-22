package com.officesupplyms.controller;

import com.officesupplyms.model.dto.ApplicationDTO;
import com.officesupplyms.model.dto.ApproveDTO;
import com.officesupplyms.model.vo.ApplicationVO;
import com.officesupplyms.service.ApplicationService;
import com.officesupplyms.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 申领管理控制器
 */
@RestController
@RequestMapping("/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    /**
     * 创建申领单
     */
    @PostMapping
    public Result<ApplicationVO> createApplication(@Validated @RequestBody ApplicationDTO applicationDTO) {
        return Result.success("申领申请已提交", applicationService.createApplication(applicationDTO));
    }

    /**
     * 审批申领单
     */
    @PutMapping("/approve")
    public Result<ApplicationVO> approveApplication(@Validated @RequestBody ApproveDTO approveDTO) {
        return Result.success("审批操作完成", applicationService.approveApplication(approveDTO));
    }

    /**
     * 出库操作
     */
    @PutMapping("/{applicationId}/outbound")
    public Result<ApplicationVO> outboundApplication(@PathVariable Long applicationId) {
        return Result.success("出库操作完成", applicationService.outboundApplication(applicationId));
    }

    /**
     * 根据ID查询申领单
     */
    @GetMapping("/{applicationId}")
    public Result<ApplicationVO> getApplicationById(@PathVariable Long applicationId) {
        return Result.success(applicationService.getApplicationById(applicationId));
    }

    /**
     * 根据申领单号查询
     */
    @GetMapping("/no/{applicationNo}")
    public Result<ApplicationVO> getApplicationByNo(@PathVariable String applicationNo) {
        return Result.success(applicationService.getApplicationByNo(applicationNo));
    }

    /**
     * 查询当前用户的申领记录
     */
    @GetMapping("/my")
    public Result<List<ApplicationVO>> getMyApplications() {
        return Result.success(applicationService.getMyApplications());
    }

    /**
     * 查询所有申领记录（管理员）
     */
    @GetMapping("/all")
    public Result<List<ApplicationVO>> getAllApplications() {
        return Result.success(applicationService.getAllApplications());
    }

    /**
     * 根据状态查询申领记录
     */
    @GetMapping("/status/{status}")
    public Result<List<ApplicationVO>> getApplicationsByStatus(@PathVariable String status) {
        return Result.success(applicationService.getApplicationsByStatus(status));
    }

    /**
     * 根据时间范围查询申领记录
     */
    @GetMapping("/time-range")
    public Result<List<ApplicationVO>> getApplicationsByTimeRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(applicationService.getApplicationsByTimeRange(startTime, endTime));
    }

    /**
     * 获取待审批申领单列表
     */
    @GetMapping("/pending")
    public Result<List<ApplicationVO>> getPendingApplications() {
        return Result.success(applicationService.getPendingApplications());
    }

    /**
     * 获取待出库申领单列表
     */
    @GetMapping("/to-be-outbound")
    public Result<List<ApplicationVO>> getToBeOutboundApplications() {
        return Result.success(applicationService.getToBeOutboundApplications());
    }

    /**
     * 获取异常申领记录
     */
    @GetMapping("/abnormal")
    public Result<List<ApplicationVO>> getAbnormalApplications() {
        return Result.success(applicationService.getAbnormalApplications());
    }

    /**
     * 统计各物资申领数量TOP10
     */
    @GetMapping("/stats/top10-materials")
    public Result<List<Map<String, Object>>> getTop10MaterialsByApplication() {
        return Result.success(applicationService.getTop10MaterialsByApplication());
    }

    /**
     * 统计每月申领单数量与金额
     */
    @GetMapping("/stats/monthly")
    public Result<List<Map<String, Object>>> getMonthlyApplicationStats() {
        return Result.success(applicationService.getMonthlyApplicationStats());
    }

    /**
     * 检测申领是否异常
     */
    @GetMapping("/detect-anomaly")
    public Result<Boolean> detectAnomaly(@RequestParam Long userId,
                                         @RequestParam Long materialId,
                                         @RequestParam Integer quantity) {
        return Result.success(applicationService.detectAnomaly(userId, materialId, quantity));
    }
}