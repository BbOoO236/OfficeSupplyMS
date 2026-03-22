package com.officesupplyms.controller;

import com.officesupplyms.model.dto.StockOutDTO;
import com.officesupplyms.model.vo.StockOutVO;
import com.officesupplyms.service.StockOutService;
import com.officesupplyms.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 出库管理控制器
 */
@RestController
@RequestMapping("/stock-outs")
public class StockOutController {

    @Autowired
    private StockOutService stockOutService;

    /**
     * 创建出库单
     */
    @PostMapping
    public Result<StockOutVO> createStockOut(@Validated @RequestBody StockOutDTO stockOutDTO) {
        return Result.success("出库单创建成功", stockOutService.createStockOut(stockOutDTO));
    }

    /**
     * 根据ID查询出库单
     */
    @GetMapping("/{stockOutId}")
    public Result<StockOutVO> getStockOutById(@PathVariable Long stockOutId) {
        return Result.success(stockOutService.getStockOutById(stockOutId));
    }

    /**
     * 根据出库单号查询
     */
    @GetMapping("/no/{stockOutNo}")
    public Result<StockOutVO> getStockOutByNo(@PathVariable String stockOutNo) {
        return Result.success(stockOutService.getStockOutByNo(stockOutNo));
    }

    /**
     * 查询所有出库单
     */
    @GetMapping
    public Result<List<StockOutVO>> getAllStockOuts() {
        return Result.success(stockOutService.getAllStockOuts());
    }

    /**
     * 根据物资ID查询出库记录
     */
    @GetMapping("/material/{materialId}")
    public Result<List<StockOutVO>> getStockOutsByMaterialId(@PathVariable Long materialId) {
        return Result.success(stockOutService.getStockOutsByMaterialId(materialId));
    }

    /**
     * 根据出库类型查询出库记录
     */
    @GetMapping("/type/{type}")
    public Result<List<StockOutVO>> getStockOutsByType(@PathVariable String type) {
        return Result.success(stockOutService.getStockOutsByType(type));
    }

    /**
     * 根据申领单ID查询出库记录
     */
    @GetMapping("/application/{applicationId}")
    public Result<StockOutVO> getStockOutByApplicationId(@PathVariable Long applicationId) {
        return Result.success(stockOutService.getStockOutByApplicationId(applicationId));
    }

    /**
     * 根据时间范围查询出库记录
     */
    @GetMapping("/time-range")
    public Result<List<StockOutVO>> getStockOutsByTimeRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(stockOutService.getStockOutsByTimeRange(startTime, endTime));
    }

    /**
     * 查询最近的出库记录
     */
    @GetMapping("/recent")
    public Result<List<StockOutVO>> getRecentStockOuts(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(stockOutService.getRecentStockOuts(limit));
    }

    /**
     * 统计指定物资的总出库数量
     */
    @GetMapping("/material/{materialId}/total-quantity")
    public Result<Integer> sumQuantityByMaterialId(@PathVariable Long materialId) {
        return Result.success(stockOutService.sumQuantityByMaterialId(materialId));
    }
}