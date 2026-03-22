package com.officesupplyms.service;

import com.officesupplyms.model.dto.MaterialDTO;
import com.officesupplyms.model.vo.MaterialVO;

import java.util.List;

/**
 * 物资服务接口
 */
public interface MaterialService {

    /**
     * 创建物资
     */
    MaterialVO createMaterial(MaterialDTO materialDTO);

    /**
     * 更新物资
     */
    MaterialVO updateMaterial(Long materialId, MaterialDTO materialDTO);

    /**
     * 删除物资（逻辑删除）
     */
    boolean deleteMaterial(Long materialId);

    /**
     * 根据ID查询物资
     */
    MaterialVO getMaterialById(Long materialId);

    /**
     * 根据编码查询物资
     */
    MaterialVO getMaterialByCode(String code);

    /**
     * 查询所有物资
     */
    List<MaterialVO> getAllMaterials();

    /**
     * 根据类别查询物资
     */
    List<MaterialVO> getMaterialsByCategory(String category);

    /**
     * 根据ABC分类查询物资
     */
    List<MaterialVO> getMaterialsByAbcClass(String abcClass);

    /**
     * 查询库存低于安全库存的物资
     */
    List<MaterialVO> getLowStockMaterials();

    /**
     * 查询需要补货的物资
     */
    List<MaterialVO> getNeedReorderMaterials();

    /**
     * 更新库存
     */
    boolean updateStock(Long materialId, Integer quantity);

    /**
     * 入库操作
     */
    boolean stockIn(Long materialId, Integer quantity, java.math.BigDecimal unitPrice);

    /**
     * 出库操作
     */
    boolean stockOut(Long materialId, Integer quantity);

    /**
     * 获取物资的日均消耗量
     */
    java.math.BigDecimal getDailyConsumption(Long materialId);

    /**
     * 计算物资的安全库存
     */
    Integer calculateSafetyStock(Long materialId);

    /**
     * 计算物资的补货点
     */
    Integer calculateReorderPoint(Long materialId);
}