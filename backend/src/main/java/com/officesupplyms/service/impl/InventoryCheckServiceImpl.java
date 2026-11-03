package com.officesupplyms.service.impl;

import com.officesupplyms.mapper.InventoryCheckMapper;
import com.officesupplyms.mapper.MaterialMapper;
import com.officesupplyms.mapper.UserMapper;
import com.officesupplyms.model.dto.InventoryCheckDTO;
import com.officesupplyms.model.entity.InventoryCheck;
import com.officesupplyms.model.entity.Material;
import com.officesupplyms.model.entity.User;
import com.officesupplyms.model.vo.InventoryCheckVO;
import com.officesupplyms.model.vo.MaterialCheckSummaryVO;
import com.officesupplyms.service.InventoryCheckService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

        // 获取账面数量（当前库存）
        Integer bookQuantity = material.getCurrentStock();
        Integer actualQuantity = inventoryCheckDTO.getActualQuantity();
        Integer difference = actualQuantity - bookQuantity;

        // 查找该物资的最新盘点记录
        List<InventoryCheck> existingChecks = inventoryCheckMapper.selectByMaterialId(inventoryCheckDTO.getMaterialId());
        InventoryCheck latestCheck = null;
        if (existingChecks != null && !existingChecks.isEmpty()) {
            existingChecks.sort((a, b) -> b.getCheckTime().compareTo(a.getCheckTime()));
            latestCheck = existingChecks.get(0);
        }

        if (latestCheck != null) {
            // 更新现有记录
            latestCheck.setBookQuantity(bookQuantity);
            latestCheck.setActualQuantity(actualQuantity);
            latestCheck.setDifference(difference);
            latestCheck.setCheckTime(LocalDateTime.now());
            latestCheck.setOperatorId(currentUserId);
            latestCheck.setReason(inventoryCheckDTO.getReason());
            latestCheck.setRemark(inventoryCheckDTO.getRemark());
            // 有差异则未调整，无差异则正常
            latestCheck.setAdjustment(difference == 0 ? 1 : 0);
            inventoryCheckMapper.updateById(latestCheck);
            return convertToVO(latestCheck);
        }

        // 生成盘点单号
        String checkNo = generateCheckNo();

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
        // 有差异则未调整，无差异则正常
        inventoryCheck.setAdjustment(difference == 0 ? 1 : 0);
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
    public boolean adjustStockByCheck(Long checkId, Integer actualQuantity, String adjustmentRemark) {
        InventoryCheck inventoryCheck = inventoryCheckMapper.selectById(checkId);
        if (inventoryCheck == null) {
            throw new RuntimeException("盘点记录不存在");
        }

        if (adjustmentRemark == null || adjustmentRemark.trim().isEmpty()) {
            throw new RuntimeException("调整备注不能为空");
        }

        // 账面数量（当前库存）
        Integer bookQuantity = inventoryCheck.getBookQuantity();
        // 计算差异 = 账面 - 实际
        Integer difference = bookQuantity - actualQuantity;

        // 只更新盘点记录，不修改物资库存
        // 物资库存的变化必须通过入库/出库操作
        inventoryCheck.setActualQuantity(actualQuantity);
        inventoryCheck.setDifference(difference);
        inventoryCheck.setRemark(adjustmentRemark);
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

    @Override
    public List<MaterialCheckSummaryVO> getMaterialCheckSummaryList() {
        List<Material> materials = materialMapper.selectList(null);
        List<MaterialCheckSummaryVO> result = new ArrayList<>();

        for (Material material : materials) {
            MaterialCheckSummaryVO summary = getMaterialCheckSummary(material.getId());
            if (summary != null) {
                result.add(summary);
            }
        }

        // 按盘点时间倒序
        result.sort((a, b) -> {
            if (a.getCheckTime() == null && b.getCheckTime() == null) return 0;
            if (a.getCheckTime() == null) return 1;
            if (b.getCheckTime() == null) return -1;
            return b.getCheckTime().compareTo(a.getCheckTime());
        });

        return result;
    }

    @Override
    public MaterialCheckSummaryVO getMaterialCheckSummary(Long materialId) {
        Material material = materialMapper.selectById(materialId);
        if (material == null) {
            return null;
        }

        List<InventoryCheck> checks = inventoryCheckMapper.selectByMaterialId(materialId);
        if (checks == null || checks.isEmpty()) {
            return null;
        }

        // 按时间倒序
        checks.sort((a, b) -> b.getCheckTime().compareTo(a.getCheckTime()));

        // 最新记录
        InventoryCheck latest = checks.get(0);

        MaterialCheckSummaryVO vo = new MaterialCheckSummaryVO();
        vo.setMaterialId(material.getId());
        vo.setMaterialName(material.getName());
        vo.setMaterialCode(material.getCode());
        vo.setMaterialUnit(material.getUnit());
        vo.setLastCheckId(latest.getId());
        vo.setLastCheckNo(latest.getCheckNo());
        vo.setBookQuantity(latest.getBookQuantity());
        vo.setActualQuantity(latest.getActualQuantity());
        vo.setDifference(latest.getDifference());
        vo.setCheckTime(latest.getCheckTime());
        vo.setAdjustment(latest.getAdjustment());
        vo.setRemark(latest.getRemark());

        // 构建历史记录：全部有差异记录 + 最后一条无差异记录
        List<InventoryCheckVO> historyList = new ArrayList<>();
        InventoryCheck lastNoDiff = null;

        for (InventoryCheck check : checks) {
            if (check.getDifference() != 0) {
                historyList.add(convertToVO(check));
            } else if (lastNoDiff == null) {
                lastNoDiff = check; // 保留第一条无差异记录
            }
            // 跳过第二条及以后的无差异记录
        }

        // 添加最后一条无差异记录（如果没有有差异记录）
        if (historyList.isEmpty() && lastNoDiff != null) {
            historyList.add(convertToVO(lastNoDiff));
        }

        // 按时间倒序排列
        historyList.sort((a, b) -> {
            if (a.getCheckTime() == null && b.getCheckTime() == null) return 0;
            if (a.getCheckTime() == null) return 1;
            if (b.getCheckTime() == null) return -1;
            return b.getCheckTime().compareTo(a.getCheckTime());
        });

        vo.setHistoryList(historyList);

        return vo;
    }

    @Override
    @Transactional
    public void updateCheckOnStockChange(Long materialId, Integer oldStock, Integer newStock) {
        List<InventoryCheck> checks = inventoryCheckMapper.selectByMaterialId(materialId);

        if (checks == null || checks.isEmpty()) {
            // 没有盘点记录，创建一个新的
            // 账面 = 新库存，实际 = 旧库存，差异 = 0（因为刚创建，还没实际盘点）
            InventoryCheck newCheck = new InventoryCheck();
            newCheck.setCheckNo(generateCheckNo());
            newCheck.setMaterialId(materialId);
            newCheck.setBookQuantity(newStock);
            newCheck.setActualQuantity(oldStock);
            newCheck.setDifference(0);
            newCheck.setCheckTime(LocalDateTime.now());
            newCheck.setOperatorId(1L);
            newCheck.setAdjustment(1); // 正常
            newCheck.setRemark("库存变化自动创建");
            inventoryCheckMapper.insert(newCheck);
        } else {
            // 已有盘点记录，更新账面数量
            // 按时间倒序，获取最新一条
            checks.sort((a, b) -> b.getCheckTime().compareTo(a.getCheckTime()));
            InventoryCheck latest = checks.get(0);

            latest.setBookQuantity(newStock);
            latest.setDifference(newStock - latest.getActualQuantity());
            latest.setCheckTime(LocalDateTime.now());
            // 如果差异为0，状态改为正常；否则保持原状态（可能是未调整）
            if (latest.getDifference() == 0) {
                latest.setAdjustment(1);
            }
            inventoryCheckMapper.updateById(latest);
        }
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