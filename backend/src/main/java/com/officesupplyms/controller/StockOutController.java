package com.officesupplyms.controller;

import com.officesupplyms.model.dto.StockOutDTO;
import com.officesupplyms.model.vo.StockOutVO;
import com.officesupplyms.model.vo.PageResult;
import com.officesupplyms.service.StockOutService;
import com.officesupplyms.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
     * 查询所有出库单（支持按出库单号、物资名称、出库类型、时间范围过滤）
     */
    @GetMapping
    public Result<PageResult<StockOutVO>> getAllStockOuts(
            @RequestParam(required = false) String stockOutNo,
            @RequestParam(required = false) String materialName,
            @RequestParam(required = false) String materialCode,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) Long page,
            @RequestParam(required = false) Long limit) {
        List<StockOutVO> stockOuts = stockOutService.getAllStockOuts();
        // 按出库单号过滤
        if (stockOutNo != null && !stockOutNo.isEmpty()) {
            final String no = stockOutNo;
            stockOuts = stockOuts.stream()
                    .filter(s -> s.getStockOutNo() != null && s.getStockOutNo().contains(no))
                    .collect(Collectors.toList());
        }
        // 按物资名称过滤
        if (materialName != null && !materialName.isEmpty()) {
            final String name = materialName;
            stockOuts = stockOuts.stream()
                    .filter(s -> s.getMaterialName() != null && s.getMaterialName().contains(name))
                    .collect(Collectors.toList());
        }
        // 按物资编码过滤
        if (materialCode != null && !materialCode.isEmpty()) {
            final String code = materialCode;
            stockOuts = stockOuts.stream()
                    .filter(s -> s.getMaterialCode() != null && s.getMaterialCode().contains(code))
                    .collect(Collectors.toList());
        }
        // 按出库类型过滤
        if (type != null && !type.isEmpty()) {
            final String t = type;
            stockOuts = stockOuts.stream()
                    .filter(s -> t.equals(s.getType()))
                    .collect(Collectors.toList());
        }
        // 按时间范围过滤
        if (startTime != null && endTime != null) {
            stockOuts = stockOuts.stream()
                    .filter(s -> s.getOutTime() != null &&
                            (s.getOutTime().isEqual(startTime) || s.getOutTime().isAfter(startTime)) &&
                            (s.getOutTime().isEqual(endTime) || s.getOutTime().isBefore(endTime)))
                    .collect(Collectors.toList());
        }

        // 记录总数
        long total = stockOuts.size();

        // 应用分页
        if (page != null && limit != null && limit > 0) {
            int start = (int) ((page - 1) * limit);
            int end = (int) Math.min(start + limit, stockOuts.size());
            if (start < stockOuts.size()) {
                stockOuts = stockOuts.subList(start, end);
            } else {
                stockOuts = new java.util.ArrayList<>();
            }
        }

        return Result.success(PageResult.of(stockOuts, total));
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

    /**
     * 删除出库单
     */
    @DeleteMapping("/{stockOutId}")
    public Result<Void> deleteStockOut(@PathVariable Long stockOutId) {
        stockOutService.deleteStockOut(stockOutId);
        return Result.success("出库单删除成功", null);
    }

    /**
     * 更新出库单
     */
    @PutMapping("/{stockOutId}")
    public Result<StockOutVO> updateStockOut(@PathVariable Long stockOutId, @Validated @RequestBody StockOutDTO stockOutDTO) {
        return Result.success("出库单更新成功", stockOutService.updateStockOut(stockOutId, stockOutDTO));
    }
}