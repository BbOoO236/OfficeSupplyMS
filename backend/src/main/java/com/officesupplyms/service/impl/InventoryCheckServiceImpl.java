package com.officesupplyms.service.impl;

import com.officesupplyms.mapper.InventoryCheckMapper;
import com.officesupplyms.mapper.MaterialMapper;
import com.officesupplyms.mapper.UserMapper;
import com.officesupplyms.model.dto.InventoryCheckDTO;
import com.officesupplyms.model.entity.InventoryCheck;
import com.officesupplyms.model.entity.Material;
import com.officesupplyms.model.entity.User;
import com.officesupplyms.model.vo.InventoryCheckVO;
import com.officesupplyms.service.InventoryCheckService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 库存盘点服务实现类
 */
@Service
public class InventoryCheckServiceImpl implements InventoryCheckService {

    @Autowired
    private InventoryCheckMapper inventoryCheckMapper;

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private UserMapper userMapper;

    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    @Transactional
    public InventoryCheckVO createInventoryCheck(InventoryCheckDTO inventoryCheckDTO) {
        // 验证物资是否存在
        Material material = materialMapper.selectById(inventoryCheckDTO.getMaterialId());
        if (material == null) {
            throw new RuntimeException("物资不存在");
        }

        // 获取当前用户ID（操作人）
        Long currentUserId = getCurrentUserId();
        User operator = userMapper.selectById(currentUserId);
        if (operator == null) {
            throw new RuntimeException("操作人不存在");
        }

        // 生成盘点单号
        String checkNo = generateCheckNo();

        // 获取账面数量（当前库存）
        Integer bookQuantity = material.getCurrentStock();
        Integer actualQuantity = inventoryCheckDTO.getActualQuantity();
        Integer difference = actualQuantity - bookQuantity;

        // 创建盘点记录
        InventoryCheck inventoryCheck = new InventoryCheck();
        inventoryCheck.setCheckNo(checkNo);
        inventoryCheck.setMaterialId(inventoryCheckDTO.getMaterialId());
        inventoryCheck.setBookQuantity(bookQuantity);
        inventoryCheck.setActualQuantity(actualQuantity);
        inventoryCheck.setDifference(difference);
        inventoryCheck.setCheckTime(LocalDateTime.now());
        inventoryCheck.setOperatorId(currentUserId);
        inventoryCheck.setReason(inventoryCheckDTO.getReason());
        inventoryCheck.setAdjustment(0); // 未调整
        inventoryCheck.setRemark(inventoryCheckDTO.getRemark());

        inventoryCheckMapper.insert(inventoryCheck);

        return convertToVO(inventoryCheck);
    }

    @Override
    public InventoryCheckVO getInventoryCheckById(Long checkId) {
        InventoryCheck inventoryCheck = inventoryCheckMapper.selectById(checkId);
        if (inventoryCheck == null) {
            return null;
        }
        return convertToVO(inventoryCheck);
    }

    @Override
    public InventoryCheckVO getInventoryCheckByNo(String checkNo) {
        InventoryCheck inventoryCheck = inventoryCheckMapper.selectByCheckNo(checkNo);
        if (inventoryCheck == null) {
            return null;
        }
        return convertToVO(inventoryCheck);
    }

    @Override
    public List<InventoryCheckVO> getAllInventoryChecks() {
        List<InventoryCheck> checks = inventoryCheckMapper.selectList(null);
        return checks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryCheckVO> getInventoryChecksByMaterialId(Long materialId) {
        List<InventoryCheck> checks = inventoryCheckMapper.selectByMaterialId(materialId);
        return checks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryCheckVO> getInventoryChecksByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        List<InventoryCheck> checks = inventoryCheckMapper.selectByTimeRange(startTime, endTime);
        return checks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryCheckVO> getUnadjustedInventoryChecks() {
        List<InventoryCheck> checks = inventoryCheckMapper.selectUnadjusted();
        return checks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryCheckVO> getInventoryChecksWithDifference() {
        List<InventoryCheck> checks = inventoryCheckMapper.selectWithDifference();
        return checks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean markAsAdjusted(Long checkId) {
        InventoryCheck inventoryCheck = inventoryCheckMapper.selectById(checkId);
        if (inventoryCheck == null) {
            return false;
        }

        inventoryCheck.setAdjustment(1);
        inventoryCheckMapper.updateById(inventoryCheck);
        return true;
    }

    @Override
    @Transactional
    public boolean adjustStockByCheck(Long checkId) {
        InventoryCheck inventoryCheck = inventoryCheckMapper.selectById(checkId);
        if (inventoryCheck == null) {
            return false;
        }

        // 如果差异为0，无需调整
        if (inventoryCheck.getDifference() == 0) {
            markAsAdjusted(checkId);
            return true;
        }

        // 调整库存：根据差异数量更新库存
        boolean updateSuccess = materialMapper.updateStock(inventoryCheck.getMaterialId(),
                inventoryCheck.getDifference()) > 0;
        if (!updateSuccess) {
            return false;
        }

        // 标记为已调整
        inventoryCheck.setAdjustment(1);
        inventoryCheckMapper.updateById(inventoryCheck);
        return true;
    }

    @Override
    public List<InventoryCheckVO> getRecentInventoryChecks(Integer limit) {
        List<InventoryCheck> checks = inventoryCheckMapper.selectRecent(limit);
        return checks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public Integer sumDifferenceByMaterialId(Long materialId) {
        return inventoryCheckMapper.sumDifferenceByMaterialId(materialId);
    }

    private String generateCheckNo() {
        // 格式: CHK + 年月日时分秒 + 随机数
        String timePart = LocalDateTime.now().format(NO_FORMATTER);
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return "CHK" + timePart + randomPart;
    }

    private Long getCurrentUserId() {
        // 简化处理：从JWT token或SecurityContext获取
        // 这里暂时返回1（管理员ID）
        return 1L;
    }

    private InventoryCheckVO convertToVO(InventoryCheck inventoryCheck) {
        InventoryCheckVO vo = new InventoryCheckVO();
        BeanUtils.copyProperties(inventoryCheck, vo);

        // 查询关联的物资信息
        Material material = materialMapper.selectById(inventoryCheck.getMaterialId());
        if (material != null) {
            vo.setMaterialName(material.getName());
            vo.setMaterialCode(material.getCode());
            vo.setMaterialUnit(material.getUnit());
        }

        // 查询操作人信息
        User operator = userMapper.selectById(inventoryCheck.getOperatorId());
        if (operator != null) {
            vo.setOperatorName(operator.getRealName());
        }

        return vo;
    }
}