package com.officesupplyms.controller;

import com.officesupplyms.model.dto.MaterialDTO;
import com.officesupplyms.model.vo.MaterialVO;
import com.officesupplyms.model.vo.PageResult;
import com.officesupplyms.service.MaterialService;
import com.officesupplyms.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物资管理控制器
 */
@RestController
@RequestMapping("/materials")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    /**
     * 创建物资
     */
    @PostMapping
    public Result<MaterialVO> createMaterial(@Validated @RequestBody MaterialDTO materialDTO) {
        return Result.success("创建物资成功", materialService.createMaterial(materialDTO));
    }

    /**
     * 更新物资
     */
    @PutMapping("/{materialId}")
    public Result<MaterialVO> updateMaterial(@PathVariable Long materialId,
                                             @Validated @RequestBody MaterialDTO materialDTO) {
        return Result.success("更新物资成功", materialService.updateMaterial(materialId, materialDTO));
    }

    /**
     * 删除物资
     */
    @DeleteMapping("/{materialId}")
    public Result<Void> deleteMaterial(@PathVariable Long materialId) {
        boolean success = materialService.deleteMaterial(materialId);
        return success ? Result.success("删除物资成功") : Result.error("删除物资失败");
    }

    /**
     * 更新物资状态（启用/停用）
     */
    @PutMapping("/{materialId}/status")
    public Result<Void> updateMaterialStatus(@PathVariable Long materialId,
                                              @RequestParam Integer status) {
        boolean success = materialService.updateMaterialStatus(materialId, status);
        return success ? Result.<Void>success(status == 1 ? "启用成功" : "停用成功")
                       : Result.<Void>error("操作失败");
    }

    /**
     * 根据ID查询物资
     */
    @GetMapping("/{materialId}")
    public Result<MaterialVO> getMaterialById(@PathVariable Long materialId) {
        return Result.success(materialService.getMaterialById(materialId));
    }

    /**
     * 根据编码查询物资
     */
    @GetMapping("/code/{code}")
    public Result<MaterialVO> getMaterialByCode(@PathVariable String code) {
        return Result.success(materialService.getMaterialByCode(code));
    }

    /**
     * 查询所有物资（支持搜索过滤）
     */
    @GetMapping
    public Result<PageResult<MaterialVO>> getAllMaterials(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String abcClass,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(required = false) String materialName,
            @RequestParam(required = false) String materialCode,
            @RequestParam(required = false) Long page,
            @RequestParam(required = false) Long limit) {
        // 兼容 materialName/materialCode 参数
        String searchName = (name != null && !name.isEmpty()) ? name : materialName;
        String searchCode = (code != null && !code.isEmpty()) ? code : materialCode;
        return Result.success(materialService.getAllMaterials(searchName, searchCode, category, abcClass, stockStatus, page, limit));
    }

    /**
     * 获取所有物资类别（下拉框用）
     */
    @GetMapping("/categories")
    public Result<List<String>> getAllCategories() {
        return Result.success(materialService.getAllCategories());
    }

    /**
     * 获取所有物资编码（下拉框用）
     */
    @GetMapping("/codes")
    public Result<List<String>> getAllCodes() {
        return Result.success(materialService.getAllCodes());
    }

    /**
     * 根据类别查询物资
     */
    @GetMapping("/category/{category}")
    public Result<List<MaterialVO>> getMaterialsByCategory(@PathVariable String category) {
        return Result.success(materialService.getMaterialsByCategory(category));
    }

    /**
     * 根据ABC分类查询物资
     */
    @GetMapping("/abc-class/{abcClass}")
    public Result<List<MaterialVO>> getMaterialsByAbcClass(@PathVariable String abcClass) {
        return Result.success(materialService.getMaterialsByAbcClass(abcClass));
    }

    /**
     * 查询库存低于安全库存的物资
     */
    @GetMapping("/low-stock")
    public Result<List<MaterialVO>> getLowStockMaterials() {
        return Result.success(materialService.getLowStockMaterials());
    }

    /**
     * 查询需要补货的物资
     */
    @GetMapping("/need-reorder")
    public Result<List<MaterialVO>> getNeedReorderMaterials() {
        return Result.success(materialService.getNeedReorderMaterials());
    }

    /**
     * 入库操作
     */
    @PostMapping("/{materialId}/stock-in")
    public Result<Void> stockIn(@PathVariable Long materialId,
                                @RequestParam Integer quantity,
                                @RequestParam java.math.BigDecimal unitPrice) {
        boolean success = materialService.stockIn(materialId, quantity, unitPrice);
        return success ? Result.<Void>success("入库成功") : Result.<Void>error("入库失败");
    }

    /**
     * 出库操作
     */
    @PostMapping("/{materialId}/stock-out")
    public Result<Void> stockOut(@PathVariable Long materialId,
                                 @RequestParam Integer quantity) {
        boolean success = materialService.stockOut(materialId, quantity);
        return success ? Result.<Void>success("出库成功") : Result.<Void>error("出库失败");
    }

    /**
     * 直接更新库存（增加或减少）
     */
    @PutMapping("/{materialId}/stock")
    public Result<Void> updateStock(@PathVariable Long materialId,
                                    @RequestParam Integer quantity) {
        boolean success = materialService.updateStock(materialId, quantity);
        return success ? Result.<Void>success("库存更新成功") : Result.<Void>error("库存更新失败");
    }

    /**
     * 获取物资的日均消耗量
     */
    @GetMapping("/{materialId}/daily-consumption")
    public Result<java.math.BigDecimal> getDailyConsumption(@PathVariable Long materialId) {
        return Result.success(materialService.getDailyConsumption(materialId));
    }

    /**
     * 计算物资的安全库存
     */
    @GetMapping("/{materialId}/safety-stock")
    public Result<Integer> calculateSafetyStock(@PathVariable Long materialId) {
        return Result.success(materialService.calculateSafetyStock(materialId));
    }

    /**
     * 计算物资的补货点
     */
    @GetMapping("/{materialId}/reorder-point")
    public Result<Integer> calculateReorderPoint(@PathVariable Long materialId) {
        return Result.success(materialService.calculateReorderPoint(materialId));
    }
}