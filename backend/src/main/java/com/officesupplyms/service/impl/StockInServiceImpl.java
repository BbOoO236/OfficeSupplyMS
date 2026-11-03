package com.officesupplyms.service.impl;

import com.officesupplyms.mapper.MaterialMapper;
import com.officesupplyms.mapper.StockInHistoryMapper;
import com.officesupplyms.mapper.StockInMapper;
import com.officesupplyms.mapper.UserMapper;
import com.officesupplyms.model.dto.StockInDTO;
import com.officesupplyms.model.entity.Material;
import com.officesupplyms.model.entity.StockIn;
import com.officesupplyms.model.entity.StockInHistory;
import com.officesupplyms.model.entity.User;
import com.officesupplyms.model.vo.StockInHistoryVO;
import com.officesupplyms.model.vo.StockInVO;
import com.officesupplyms.service.InventoryCheckService;
import com.officesupplyms.service.StockInService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 入库服务实现类
 */
@Service
public class StockInServiceImpl implements StockInService {

    @Autowired
    private StockInMapper stockInMapper;

    @Autowired
    private StockInHistoryMapper stockInHistoryMapper;

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private InventoryCheckService inventoryCheckService;

    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    @Transactional
    public StockInVO createStockIn(StockInDTO stockInDTO) {
        // 验证物资是否存在
        Material material = materialMapper.selectById(stockInDTO.getMaterialId());
        if (material == null) {
            throw new RuntimeException("物资不存在");
        }

        // 记录入库前的库存（用于盘点）
        Integer oldStock = material.getCurrentStock();

        // 获取当前用户ID（操作人）
        Long currentUserId = getCurrentUserId();
        User operator = userMapper.selectById(currentUserId);
        if (operator == null) {
            throw new RuntimeException("操作人不存在");
        }

        // 生成入库单号
        String stockInNo = generateStockInNo();

        // 计算总金额
        BigDecimal totalAmount = stockInDTO.getUnitPrice().multiply(BigDecimal.valueOf(stockInDTO.getQuantity()));

        // 创建入库单
        StockIn stockIn = new StockIn();
        stockIn.setStockInNo(stockInNo);
        stockIn.setMaterialId(stockInDTO.getMaterialId());
        stockIn.setQuantity(stockInDTO.getQuantity());
        stockIn.setUnitPrice(stockInDTO.getUnitPrice());
        stockIn.setTotalAmount(totalAmount);
        stockIn.setSupplier(stockInDTO.getSupplier());
        stockIn.setInTime(LocalDateTime.now());
        stockIn.setOperatorId(currentUserId);
        stockIn.setRemark(stockInDTO.getRemark());

        stockInMapper.insert(stockIn);

        // 记录操作历史
        recordHistory(stockIn.getId(), "CREATE", currentUserId, operator.getRealName(), "新建入库单");

        // 更新库存
        boolean updateSuccess = materialMapper.updateStock(stockInDTO.getMaterialId(), stockInDTO.getQuantity()) > 0;
        if (!updateSuccess) {
            throw new RuntimeException("库存更新失败");
        }

        // 更新物资单价（加权平均）
        updateMaterialUnitPrice(material, stockInDTO.getQuantity(), stockInDTO.getUnitPrice());

        // 自动更新库存盘点记录
        Integer newStock = oldStock + stockInDTO.getQuantity();
        inventoryCheckService.updateCheckOnStockChange(stockInDTO.getMaterialId(), oldStock, newStock);

        return convertToVO(stockIn);
    }

    @Override
    public StockInVO getStockInById(Long stockInId) {
        StockIn stockIn = stockInMapper.selectById(stockInId);
        if (stockIn == null) {
            return null;
        }
        return convertToVO(stockIn);
    }

    @Override
    public StockInVO getStockInByNo(String stockInNo) {
        StockIn stockIn = stockInMapper.selectByStockInNo(stockInNo);
        if (stockIn == null) {
            return null;
        }
        return convertToVO(stockIn);
    }

    @Override
    public List<StockInVO> getAllStockIns() {
        List<StockIn> stockIns = stockInMapper.selectList(null);
        return stockIns.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockInVO> getStockInsByMaterialId(Long materialId) {
        List<StockIn> stockIns = stockInMapper.selectByMaterialId(materialId);
        return stockIns.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockInVO> getStockInsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        List<StockIn> stockIns = stockInMapper.selectByTimeRange(startTime, endTime);
        return stockIns.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockInVO> getRecentStockIns(Integer limit) {
        List<StockIn> stockIns = stockInMapper.selectRecent(limit);
        return stockIns.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public Integer sumQuantityByMaterialId(Long materialId) {
        return stockInMapper.sumQuantityByMaterialId(materialId);
    }

    @Override
    public BigDecimal sumAmountByMaterialId(Long materialId) {
        return stockInMapper.sumAmountByMaterialId(materialId);
    }

    @Override
    @Transactional
    public boolean deleteStockIn(Long stockInId) {
        StockIn stockIn = stockInMapper.selectById(stockInId);
        if (stockIn == null) {
            throw new RuntimeException("入库单不存在");
        }

        // 获取删除前的库存（用于盘点）
        Material material = materialMapper.selectById(stockIn.getMaterialId());
        Integer oldStock = material != null ? material.getCurrentStock() : 0;

        // 还原库存（减去入库数量）
        materialMapper.updateStock(stockIn.getMaterialId(), -stockIn.getQuantity());

        // 自动更新库存盘点记录
        inventoryCheckService.updateCheckOnStockChange(stockIn.getMaterialId(), oldStock, oldStock - stockIn.getQuantity());

        return stockInMapper.deleteById(stockInId) > 0;
    }

    @Override
    @Transactional
    public StockInVO updateStockIn(Long stockInId, StockInDTO stockInDTO) {
        StockIn stockIn = stockInMapper.selectById(stockInId);
        if (stockIn == null) {
            throw new RuntimeException("入库单不存在");
        }

        // 获取当前用户
        Long currentUserId = getCurrentUserId();
        User operator = userMapper.selectById(currentUserId);

        // 如果修改了物资或数量，需要调整库存
        Integer oldQuantity = stockIn.getQuantity();
        Long oldMaterialId = stockIn.getMaterialId();
        Integer newQuantity = stockInDTO.getQuantity();
        Long newMaterialId = stockInDTO.getMaterialId();

        // 获取修改前的库存（用于盘点）
        Material oldMaterial = materialMapper.selectById(oldMaterialId);
        Integer oldStock = oldMaterial != null ? oldMaterial.getCurrentStock() : 0;

        // 还原旧库存
        materialMapper.updateStock(oldMaterialId, -oldQuantity);

        // 检查新库存是否充足（减少库存）
        Material newMaterial = materialMapper.selectById(newMaterialId);
        if (newMaterial == null) {
            throw new RuntimeException("物资不存在");
        }

        // 增加新库存
        materialMapper.updateStock(newMaterialId, newQuantity);

        // 计算新金额
        BigDecimal totalAmount = stockInDTO.getUnitPrice().multiply(BigDecimal.valueOf(newQuantity));

        // 更新入库单
        stockIn.setMaterialId(newMaterialId);
        stockIn.setQuantity(newQuantity);
        stockIn.setUnitPrice(stockInDTO.getUnitPrice());
        stockIn.setTotalAmount(totalAmount);
        stockIn.setSupplier(stockInDTO.getSupplier());
        stockIn.setRemark(stockInDTO.getRemark());

        stockInMapper.updateById(stockIn);

        // 记录操作历史
        String remark = "修改入库单";
        if (!oldMaterialId.equals(newMaterialId)) {
            remark += "，物资由[" + oldMaterial.getName() + "]改为[" + newMaterial.getName() + "]";
        }
        if (!oldQuantity.equals(newQuantity)) {
            remark += "，数量由[" + oldQuantity + "]改为[" + newQuantity + "]";
        }
        recordHistory(stockIn.getId(), "UPDATE", currentUserId, operator != null ? operator.getRealName() : "-", remark);

        // 自动更新库存盘点记录（原物资）
        inventoryCheckService.updateCheckOnStockChange(oldMaterialId, oldStock, oldStock - oldQuantity);

        // 如果物资也换了，也更新新物资的盘点
        if (!oldMaterialId.equals(newMaterialId)) {
            Integer newOldStock = newMaterial.getCurrentStock() - newQuantity;
            inventoryCheckService.updateCheckOnStockChange(newMaterialId, newOldStock, newMaterial.getCurrentStock());
        }

        return convertToVO(stockIn);
    }

    private String generateStockInNo() {
        // 格式: IN + 年月日时分秒 + 随机数
        String timePart = LocalDateTime.now().format(NO_FORMATTER);
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return "IN" + timePart + randomPart;
    }

    private void updateMaterialUnitPrice(Material material, Integer quantity, BigDecimal newUnitPrice) {
        // 入库前的库存
        Integer currentStockVal = material.getCurrentStock();
        BigDecimal currentUnitPrice = material.getUnitPrice();

        // 新物资首次入库（或无历史单价），直接使用新单价
        if (currentStockVal == null || currentStockVal == 0 || currentUnitPrice == null) {
            material.setUnitPrice(newUnitPrice);
            // 只更新单价字段，不更新库存（避免覆盖之前updateStock的更新）
            materialMapper.updateUnitPriceOnly(material.getId(), newUnitPrice);
            return;
        }

        // 加权平均计算新单价
        BigDecimal currentStock = BigDecimal.valueOf(currentStockVal);
        BigDecimal currentTotal = currentUnitPrice.multiply(currentStock);
        BigDecimal newTotal = newUnitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal totalStock = currentStock.add(BigDecimal.valueOf(quantity));

        if (totalStock.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal weightedPrice = currentTotal.add(newTotal).divide(totalStock, 2, BigDecimal.ROUND_HALF_UP);
            // 只更新单价字段，不更新库存（避免覆盖之前updateStock的更新）
            materialMapper.updateUnitPriceOnly(material.getId(), weightedPrice);
        }
    }

    private Long getCurrentUserId() {
        // 简化处理：从JWT token或SecurityContext获取
        // 这里暂时返回1（管理员ID）
        return 1L;
    }

    private void recordHistory(Long stockInId, String action, Long operatorId, String operatorName, String remark) {
        StockInHistory history = new StockInHistory();
        history.setStockInId(stockInId);
        history.setAction(action);
        history.setOperatorId(operatorId);
        history.setOperatorName(operatorName);
        history.setRemark(remark);
        history.setCreateTime(LocalDateTime.now());
        stockInHistoryMapper.insert(history);
    }

    private StockInVO convertToVO(StockIn stockIn) {
        StockInVO vo = new StockInVO();
        BeanUtils.copyProperties(stockIn, vo);

        // 查询关联的物资信息
        Material material = materialMapper.selectById(stockIn.getMaterialId());
        if (material != null) {
            vo.setMaterialName(material.getName());
            vo.setMaterialCode(material.getCode());
            vo.setMaterialUnit(material.getUnit());
        }

        // 查询操作人信息
        User operator = userMapper.selectById(stockIn.getOperatorId());
        if (operator != null) {
            vo.setOperatorName(operator.getRealName());
        }

        // 查询操作历史
        List<StockInHistory> histories = stockInHistoryMapper.selectByStockInId(stockIn.getId());
        List<StockInHistoryVO> historyVOList = histories.stream().map(this::convertToHistoryVO).collect(Collectors.toList());
        vo.setHistoryList(historyVOList);

        return vo;
    }

    private StockInHistoryVO convertToHistoryVO(StockInHistory history) {
        StockInHistoryVO vo = new StockInHistoryVO();
        BeanUtils.copyProperties(history, vo);
        vo.setActionText(getActionText(history.getAction()));
        return vo;
    }

    private String getActionText(String action) {
        switch (action) {
            case "CREATE": return "创建";
            case "UPDATE": return "修改";
            case "DELETE": return "删除";
            default: return action;
        }
    }
}