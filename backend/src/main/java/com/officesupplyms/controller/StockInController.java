package com.officesupplyms.controller;

import com.officesupplyms.model.dto.StockInDTO;
import com.officesupplyms.model.vo.StockInVO;
import com.officesupplyms.service.StockInService;
import com.officesupplyms.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 入库管理控制器
 */
@RestController
@RequestMapping("/stock-ins")
public class StockInController {

    @Autowired
    private StockInService stockInService;

    /**
     * 创建入库单
     */
    @PostMapping
    public Result<StockInVO> createStockIn(@Validated @RequestBody StockInDTO stockInDTO) {
        return Result.success("入库单创建成功", stockInService.createStockIn(stockInDTO));
    }

    /**
     * 根据ID查询入库单
     */
    @GetMapping("/{stockInId}")
    public Result<StockInVO> getStockInById(@PathVariable Long stockInId) {
        return Result.success(stockInService.getStockInById(stockInId));
    }

    /**
     * 根据入库单号查询
     */
    @GetMapping("/no/{stockInNo}")
    public Result<StockInVO> getStockInByNo(@PathVariable String stockInNo) {
        return Result.success(stockInService.getStockInByNo(stockInNo));
    }

    /**
     * 查询所有入库单
     */
    @GetMapping
    public Result<List<StockInVO>> getAllStockIns() {
        return Result.success(stockInService.getAllStockIns());
    }

    /**
     * 根据物资ID查询入库记录
     */
    @GetMapping("/material/{materialId}")
    public Result<List<StockInVO>> getStockInsByMaterialId(@PathVariable Long materialId) {
        return Result.success(stockInService.getStockInsByMaterialId(materialId));
    }

    /**
     * 根据时间范围查询入库记录
     */
    @GetMapping("/time-range")
    public Result<List<StockInVO>> getStockInsByTimeRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(stockInService.getStockInsByTimeRange(startTime, endTime));
    }

    /**
     * 查询最近的入库记录
     */
    @GetMapping("/recent")
    public Result<List<StockInVO>> getRecentStockIns(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(stockInService.getRecentStockIns(limit));
    }

    /**
     * 统计指定物资的总入库数量
     */
    @GetMapping("/material/{materialId}/total-quantity")
    public Result<Integer> sumQuantityByMaterialId(@PathVariable Long materialId) {
        return Result.success(stockInService.sumQuantityByMaterialId(materialId));
    }

    /**
     * 统计指定物资的总入库金额
     */
    @GetMapping("/material/{materialId}/total-amount")
    public Result<java.math.BigDecimal> sumAmountByMaterialId(@PathVariable Long materialId) {
        return Result.success(stockInService.sumAmountByMaterialId(materialId));
    }
}