package com.officesupplyms.controller;

import com.officesupplyms.model.vo.ConsumptionHistoryVO;
import com.officesupplyms.service.ConsumptionHistoryService;
import com.officesupplyms.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 历史消耗记录控制器
 */
@RestController
@RequestMapping("/consumption-histories")
public class ConsumptionHistoryController {

    @Autowired
    private ConsumptionHistoryService consumptionHistoryService;

    /**
     * 根据ID查询消耗记录
     */
    @GetMapping("/{id}")
    public Result<ConsumptionHistoryVO> getById(@PathVariable Long id) {
        ConsumptionHistoryVO vo = consumptionHistoryService.getById(id);
        return vo != null ? Result.success(vo) : Result.error("消耗记录不存在");
    }

    /**
     * 查询所有消耗记录
     */
    @GetMapping
    public Result<List<ConsumptionHistoryVO>> getAll() {
        return Result.success(consumptionHistoryService.getAll());
    }

    /**
     * 根据物资ID查询消耗记录
     */
    @GetMapping("/material/{materialId}")
    public Result<List<ConsumptionHistoryVO>> getByMaterialId(@PathVariable Long materialId) {
        return Result.success(consumptionHistoryService.getByMaterialId(materialId));
    }

    /**
     * 根据用户ID查询消耗记录
     */
    @GetMapping("/user/{userId}")
    public Result<List<ConsumptionHistoryVO>> getByUserId(@PathVariable Long userId) {
        return Result.success(consumptionHistoryService.getByUserId(userId));
    }

    /**
     * 根据时间范围查询消耗记录
     */
    @GetMapping("/time-range")
    public Result<List<ConsumptionHistoryVO>> getByTimeRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(consumptionHistoryService.getByTimeRange(startTime, endTime));
    }

    /**
     * 根据记录日期查询消耗记录
     */
    @GetMapping("/date/{recordDate}")
    public Result<List<ConsumptionHistoryVO>> getByRecordDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate recordDate) {
        return Result.success(consumptionHistoryService.getByRecordDate(recordDate));
    }

    /**
     * 根据类型查询消耗记录
     */
    @GetMapping("/type/{type}")
    public Result<List<ConsumptionHistoryVO>> getByType(@PathVariable String type) {
        return Result.success(consumptionHistoryService.getByType(type));
    }

    /**
     * 查询最近的消耗记录
     */
    @GetMapping("/recent")
    public Result<List<ConsumptionHistoryVO>> getRecent(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(consumptionHistoryService.getRecent(limit));
    }

    /**
     * 统计指定物资的总消耗数量
     */
    @GetMapping("/material/{materialId}/total-quantity")
    public Result<Integer> sumQuantityByMaterialId(@PathVariable Long materialId) {
        return Result.success(consumptionHistoryService.sumQuantityByMaterialId(materialId));
    }

    /**
     * 统计指定用户的总消耗数量
     */
    @GetMapping("/user/{userId}/total-quantity")
    public Result<Integer> sumQuantityByUserId(@PathVariable Long userId) {
        return Result.success(consumptionHistoryService.sumQuantityByUserId(userId));
    }
}