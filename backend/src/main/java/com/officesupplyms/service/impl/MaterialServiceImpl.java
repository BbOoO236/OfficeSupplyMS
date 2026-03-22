package com.officesupplyms.service.impl;

import com.officesupplyms.mapper.MaterialMapper;
import com.officesupplyms.model.dto.MaterialDTO;
import com.officesupplyms.model.entity.Material;
import com.officesupplyms.model.vo.MaterialVO;
import com.officesupplyms.service.MaterialService;
import com.officesupplyms.algorithm.AlgorithmUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 物资服务实现类
 */
@Service
public class MaterialServiceImpl implements MaterialService {

    @Autowired
    private MaterialMapper materialMapper;

    @Override
    public MaterialVO createMaterial(MaterialDTO materialDTO) {
        // 检查物资编码是否已存在
        Material existingMaterial = materialMapper.selectByCode(materialDTO.getCode());
        if (existingMaterial != null) {
            throw new RuntimeException("物资编码已存在");
        }

        Material material = new Material();
        BeanUtils.copyProperties(materialDTO, material);

        material.setCreateTime(LocalDateTime.now());
        material.setUpdateTime(LocalDateTime.now());
        material.setStatus(1); // 正常状态

        // 设置默认值
        if (material.getAbcClass() == null) {
            material.setAbcClass("C"); // 默认C类
        }
        if (material.getAnnualConsumption() == null) {
            material.setAnnualConsumption(0);
        }
        if (material.getAnnualConsumptionAmount() == null) {
            material.setAnnualConsumptionAmount(BigDecimal.ZERO);
        }

        materialMapper.insert(material);

        return convertToVO(material);
    }

    @Override
    public MaterialVO updateMaterial(Long materialId, MaterialDTO materialDTO) {
        Material material = materialMapper.selectById(materialId);
        if (material == null) {
            throw new RuntimeException("物资不存在");
        }

        // 检查物资编码是否与其他物资重复
        if (materialDTO.getCode() != null && !materialDTO.getCode().equals(material.getCode())) {
            Material existingMaterial = materialMapper.selectByCode(materialDTO.getCode());
            if (existingMaterial != null && !existingMaterial.getId().equals(materialId)) {
                throw new RuntimeException("物资编码已存在");
            }
        }

        // 更新物资信息
        if (materialDTO.getCode() != null) {
            material.setCode(materialDTO.getCode());
        }
        if (materialDTO.getName() != null) {
            material.setName(materialDTO.getName());
        }
        if (materialDTO.getCategory() != null) {
            material.setCategory(materialDTO.getCategory());
        }
        if (materialDTO.getSpecification() != null) {
            material.setSpecification(materialDTO.getSpecification());
        }
        if (materialDTO.getUnit() != null) {
            material.setUnit(materialDTO.getUnit());
        }
        if (materialDTO.getUnitPrice() != null) {
            material.setUnitPrice(materialDTO.getUnitPrice());
        }
        if (materialDTO.getLeadTime() != null) {
            material.setLeadTime(materialDTO.getLeadTime());
        }
        if (materialDTO.getSafetyFactor() != null) {
            material.setSafetyFactor(materialDTO.getSafetyFactor());
        }
        if (materialDTO.getCurrentStock() != null) {
            material.setCurrentStock(materialDTO.getCurrentStock());
        }
        if (materialDTO.getMinStock() != null) {
            material.setMinStock(materialDTO.getMinStock());
        }
        if (materialDTO.getMaxStock() != null) {
            material.setMaxStock(materialDTO.getMaxStock());
        }
        if (materialDTO.getStatus() != null) {
            material.setStatus(materialDTO.getStatus());
        }
        if (materialDTO.getRemark() != null) {
            material.setRemark(materialDTO.getRemark());
        }

        material.setUpdateTime(LocalDateTime.now());
        materialMapper.updateById(material);

        return convertToVO(material);
    }

    @Override
    public boolean deleteMaterial(Long materialId) {
        Material material = materialMapper.selectById(materialId);
        if (material == null) {
            return false;
        }

        // 逻辑删除：更新状态为停用
        material.setStatus(0);
        material.setUpdateTime(LocalDateTime.now());
        materialMapper.updateById(material);

        return true;
    }

    @Override
    public MaterialVO getMaterialById(Long materialId) {
        Material material = materialMapper.selectById(materialId);
        if (material == null) {
            return null;
        }
        return convertToVO(material);
    }

    @Override
    public MaterialVO getMaterialByCode(String code) {
        Material material = materialMapper.selectByCode(code);
        if (material == null) {
            return null;
        }
        return convertToVO(material);
    }

    @Override
    public List<MaterialVO> getAllMaterials() {
        List<Material> materials = materialMapper.selectList(null);
        return materials.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MaterialVO> getMaterialsByCategory(String category) {
        List<Material> materials = materialMapper.selectByCategory(category);
        return materials.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MaterialVO> getMaterialsByAbcClass(String abcClass) {
        List<Material> materials = materialMapper.selectByAbcClass(abcClass);
        return materials.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MaterialVO> getLowStockMaterials() {
        List<Material> materials = materialMapper.selectLowStockMaterials();
        return materials.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MaterialVO> getNeedReorderMaterials() {
        List<Material> materials = materialMapper.selectNeedReorderMaterials();
        return materials.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean updateStock(Long materialId, Integer quantity) {
        int rows = materialMapper.updateStock(materialId, quantity);
        return rows > 0;
    }

    @Override
    @Transactional
    public boolean stockIn(Long materialId, Integer quantity, BigDecimal unitPrice) {
        // 入库操作
        // 1. 更新库存
        boolean success = updateStock(materialId, quantity);
        if (!success) {
            return false;
        }

        // 2. 记录入库单（这里简化处理）
        // 3. 更新物资单价（如果入库单价与当前单价不同，可以按加权平均计算新单价）

        return true;
    }

    @Override
    @Transactional
    public boolean stockOut(Long materialId, Integer quantity) {
        // 检查库存是否充足
        Material material = materialMapper.selectById(materialId);
        if (material == null || material.getCurrentStock() < quantity) {
            throw new RuntimeException("库存不足");
        }

        // 出库操作（减少库存）
        boolean success = updateStock(materialId, -quantity);
        if (!success) {
            return false;
        }

        // 记录出库单（这里简化处理）

        return true;
    }

    @Override
    public BigDecimal getDailyConsumption(Long materialId) {
        // 计算日均消耗量（最近3个月）
        // 这里简化处理，返回固定值
        return BigDecimal.valueOf(10);
    }

    @Override
    public Integer calculateSafetyStock(Long materialId) {
        Material material = materialMapper.selectById(materialId);
        if (material == null) {
            return 0;
        }

        BigDecimal dailyConsumption = getDailyConsumption(materialId);
        return AlgorithmUtil.calculateSafetyStock(dailyConsumption,
                material.getLeadTime(), material.getSafetyFactor());
    }

    @Override
    public Integer calculateReorderPoint(Long materialId) {
        Material material = materialMapper.selectById(materialId);
        if (material == null) {
            return 0;
        }

        BigDecimal dailyConsumption = getDailyConsumption(materialId);
        int safetyStock = calculateSafetyStock(materialId);
        return AlgorithmUtil.calculateReorderPoint(dailyConsumption,
                material.getLeadTime(), safetyStock);
    }

    private MaterialVO convertToVO(Material material) {
        MaterialVO vo = new MaterialVO();
        BeanUtils.copyProperties(material, vo);

        // 计算扩展字段
        BigDecimal dailyConsumption = getDailyConsumption(material.getId());
        int safetyStock = calculateSafetyStock(material.getId());
        int reorderPoint = calculateReorderPoint(material.getId());
        boolean needReorder = material.getCurrentStock() <= reorderPoint;

        vo.setDailyConsumption(dailyConsumption);
        vo.setSafetyStock(safetyStock);
        vo.setReorderPoint(reorderPoint);
        vo.setNeedReorder(needReorder);

        // 设置库存状态
        if (material.getCurrentStock() <= material.getMinStock()) {
            vo.setStockStatus("LOW");
        } else if (material.getCurrentStock() >= material.getMaxStock()) {
            vo.setStockStatus("HIGH");
        } else {
            vo.setStockStatus("NORMAL");
        }

        return vo;
    }
}