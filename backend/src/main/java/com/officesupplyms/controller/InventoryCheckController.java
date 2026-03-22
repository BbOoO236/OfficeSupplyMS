package com.officesupplyms.controller;

import com.officesupplyms.model.dto.InventoryCheckDTO;
import com.officesupplyms.model.vo.InventoryCheckVO;
import com.officesupplyms.service.InventoryCheckService;
import com.officesupplyms.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存盘点控制器
 */
@RestController
@RequestMapping("/inventory-checks")
public class InventoryCheckController {

    @Autowired
    private InventoryCheckService inventoryCheckService;

    /**
     * 创建盘点记录
     */
    @PostMapping
    public Result<InventoryCheckVO> createInventoryCheck(@Validated @RequestBody InventoryCheckDTO inventoryCheckDTO) {
        return Result.success("盘点记录创建成功", inventoryCheckService.createInventoryCheck(inventoryCheckDTO));
    }

    /**
     * 根据ID查询盘点记录
     */
    @GetMapping("/{checkId}")
    public Result<InventoryCheckVO> getInventoryCheckById(@PathVariable Long checkId) {
        return Result.success(inventoryCheckService.getInventoryCheckById(checkId));
    }

    /**
     * 根据盘点单号查询
     */
    @GetMapping("/no/{checkNo}")
    public Result<InventoryCheckVO> getInventoryCheckByNo(@PathVariable String checkNo) {
        return Result.success(inventoryCheckService.getInventoryCheckByNo(checkNo));
    }

    /**
     * 查询所有盘点记录
     */
    @GetMapping
    public Result<List<InventoryCheckVO>> getAllInventoryChecks() {
        return Result.success(inventoryCheckService.getAllInventoryChecks());
    }

    /**
     * 根据物资ID查询盘点记录
     */
    @GetMapping("/material/{materialId}")
    public Result<List<InventoryCheckVO>> getInventoryChecksByMaterialId(@PathVariable Long materialId) {
        return Result.success(inventoryCheckService.getInventoryChecksByMaterialId(materialId));
    }

    /**
     * 根据时间范围查询盘点记录
     */
    @GetMapping("/time-range")
    public Result<List<InventoryCheckVO>> getInventoryChecksByTimeRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(inventoryCheckService.getInventoryChecksByTimeRange(startTime, endTime));
    }

    /**
     * 查询未调整的盘点记录
     */
    @GetMapping("/unadjusted")
    public Result<List<InventoryCheckVO>> getUnadjustedInventoryChecks() {
        return Result.success(inventoryCheckService.getUnadjustedInventoryChecks());
    }

    /**
     * 查询有差异的盘点记录
     */
    @GetMapping("/with-difference")
    public Result<List<InventoryCheckVO>> getInventoryChecksWithDifference() {
        return Result.success(inventoryCheckService.getInventoryChecksWithDifference());
    }

    /**
     * 更新盘点记录状态为已调整
     */
    @PutMapping("/{checkId}/mark-adjusted")
    public Result<Void> markAsAdjusted(@PathVariable Long checkId) {
        boolean success = inventoryCheckService.markAsAdjusted(checkId);
        return success ? Result.<Void>success("盘点记录已标记为已调整") : Result.<Void>error("标记失败");
    }

    /**
     * 调整库存根据盘点差异
     */
    @PutMapping("/{checkId}/adjust-stock")
    public Result<Void> adjustStockByCheck(@PathVariable Long checkId) {
        boolean success = inventoryCheckService.adjustStockByCheck(checkId);
        return success ? Result.<Void>success("库存调整成功") : Result.<Void>error("库存调整失败");
    }

    /**
     * 查询最近的盘点记录
     */
    @GetMapping("/recent")
    public Result<List<InventoryCheckVO>> getRecentInventoryChecks(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(inventoryCheckService.getRecentInventoryChecks(limit));
    }

    /**
     * 统计指定物资的盘点差异总量
     */
    @GetMapping("/material/{materialId}/total-difference")
    public Result<Integer> sumDifferenceByMaterialId(@PathVariable Long materialId) {
        return Result.success(inventoryCheckService.sumDifferenceByMaterialId(materialId));
    }
}