package com.officesupplyms.controller;

import com.officesupplyms.model.dto.AlertRecordDTO;
import com.officesupplyms.model.vo.AlertRecordVO;
import com.officesupplyms.service.AlertRecordService;
import com.officesupplyms.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预警记录控制器
 */
@RestController
@RequestMapping("/alert-records")
public class AlertRecordController {

    @Autowired
    private AlertRecordService alertRecordService;

    /**
     * 创建预警记录
     */
    @PostMapping
    public Result<Void> createAlertRecord(@Validated @RequestBody AlertRecordDTO alertRecordDTO) {
        boolean success = alertRecordService.createAlertRecord(
                alertRecordDTO.getMaterialId(),
                alertRecordDTO.getAlertType(),
                alertRecordDTO.getAlertLevel(),
                alertRecordDTO.getCurrentValue(),
                alertRecordDTO.getThreshold(),
                alertRecordDTO.getMessage()
        );
        return success ? Result.success("预警记录创建成功") : Result.error("预警记录创建失败");
    }

    /**
     * 根据ID查询预警记录
     */
    @GetMapping("/{alertId}")
    public Result<AlertRecordVO> getAlertRecordById(@PathVariable Long alertId) {
        return Result.success(alertRecordService.getAlertRecordById(alertId));
    }

    /**
     * 查询所有预警记录
     */
    @GetMapping
    public Result<List<AlertRecordVO>> getAllAlertRecords() {
        return Result.success(alertRecordService.getAllAlertRecords());
    }

    /**
     * 根据预警类型查询
     */
    @GetMapping("/type/{alertType}")
    public Result<List<AlertRecordVO>> getAlertRecordsByType(@PathVariable String alertType) {
        return Result.success(alertRecordService.getAlertRecordsByType(alertType));
    }

    /**
     * 根据预警等级查询
     */
    @GetMapping("/level/{alertLevel}")
    public Result<List<AlertRecordVO>> getAlertRecordsByLevel(@PathVariable String alertLevel) {
        return Result.success(alertRecordService.getAlertRecordsByLevel(alertLevel));
    }

    /**
     * 根据状态查询预警记录
     */
    @GetMapping("/status/{status}")
    public Result<List<AlertRecordVO>> getAlertRecordsByStatus(@PathVariable String status) {
        return Result.success(alertRecordService.getAlertRecordsByStatus(status));
    }

    /**
     * 根据物资ID查询预警记录
     */
    @GetMapping("/material/{materialId}")
    public Result<List<AlertRecordVO>> getAlertRecordsByMaterialId(@PathVariable Long materialId) {
        return Result.success(alertRecordService.getAlertRecordsByMaterialId(materialId));
    }

    /**
     * 根据时间范围查询预警记录
     */
    @GetMapping("/time-range")
    public Result<List<AlertRecordVO>> getAlertRecordsByTimeRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(alertRecordService.getAlertRecordsByTimeRange(startTime, endTime));
    }

    /**
     * 查询未读预警记录
     */
    @GetMapping("/unread")
    public Result<List<AlertRecordVO>> getUnreadAlertRecords() {
        return Result.success(alertRecordService.getUnreadAlertRecords());
    }

    /**
     * 标记预警为已读
     */
    @PutMapping("/{alertId}/mark-read")
    public Result<Void> markAsRead(@PathVariable Long alertId) {
        boolean success = alertRecordService.markAsRead(alertId);
        return success ? Result.success("预警记录已标记为已读") : Result.error("标记失败");
    }

    /**
     * 标记预警为已处理
     */
    @PutMapping("/{alertId}/mark-processed")
    public Result<Void> markAsProcessed(@PathVariable Long alertId,
                                        @RequestParam(required = false) Long processUserId) {
        boolean success = alertRecordService.markAsProcessed(alertId, processUserId);
        return success ? Result.success("预警记录已标记为已处理") : Result.error("标记失败");
    }

    /**
     * 统计未读预警数量
     */
    @GetMapping("/count-unread")
    public Result<Integer> countUnreadAlertRecords() {
        return Result.success(alertRecordService.countUnreadAlertRecords());
    }

    /**
     * 查询最近的预警记录
     */
    @GetMapping("/recent")
    public Result<List<AlertRecordVO>> getRecentAlertRecords(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(alertRecordService.getRecentAlertRecords(limit));
    }
}