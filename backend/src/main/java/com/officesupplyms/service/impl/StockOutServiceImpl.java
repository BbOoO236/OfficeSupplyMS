package com.officesupplyms.service.impl;

import com.officesupplyms.mapper.ApplicationMapper;
import com.officesupplyms.mapper.MaterialMapper;
import com.officesupplyms.mapper.StockOutHistoryMapper;
import com.officesupplyms.mapper.StockOutMapper;
import com.officesupplyms.mapper.UserMapper;
import com.officesupplyms.model.dto.StockOutDTO;
import com.officesupplyms.model.entity.Application;
import com.officesupplyms.model.entity.Material;
import com.officesupplyms.model.entity.StockOut;
import com.officesupplyms.model.entity.StockOutHistory;
import com.officesupplyms.model.entity.User;
import com.officesupplyms.model.vo.StockOutHistoryVO;
import com.officesupplyms.model.vo.StockOutVO;
import com.officesupplyms.service.InventoryCheckService;
import com.officesupplyms.service.StockOutService;
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
 * 出库服务实现类
 */
@Service
public class StockOutServiceImpl implements StockOutService {

    @Autowired
    private StockOutMapper stockOutMapper;

    @Autowired
    private StockOutHistoryMapper stockOutHistoryMapper;

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private InventoryCheckService inventoryCheckService;

    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    @Transactional
    public StockOutVO createStockOut(StockOutDTO stockOutDTO) {
        // 验证物资是否存在
        Material material = materialMapper.selectById(stockOutDTO.getMaterialId());
        if (material == null) {
            throw new RuntimeException("物资不存在");
        }

        // 记录出库前的库存（用于盘点）
        Integer oldStock = material.getCurrentStock();

        // 检查库存是否充足
        if (oldStock < stockOutDTO.getQuantity()) {
            throw new RuntimeException("库存不足");
        }

        // 获取当前用户ID（操作人）
        Long currentUserId = getCurrentUserId();
        User operator = userMapper.selectById(currentUserId);

        // 如果选择了关联申领单，验证申领单存在
        if (stockOutDTO.getApplicationId() != null) {
            Application application = applicationMapper.selectById(stockOutDTO.getApplicationId());
            if (application == null) {
                throw new RuntimeException("关联的申领单不存在");
            }
        }

        // 生成出库单号
        String stockOutNo = generateStockOutNo();

        // 创建出库单
        StockOut stockOut = new StockOut();
        stockOut.setStockOutNo(stockOutNo);
        stockOut.setMaterialId(stockOutDTO.getMaterialId());
        stockOut.setQuantity(stockOutDTO.getQuantity());
        stockOut.setType(stockOutDTO.getType());
        stockOut.setApplicationId(stockOutDTO.getApplicationId());
        stockOut.setOutTime(LocalDateTime.now());
        stockOut.setOperatorId(currentUserId);
        stockOut.setReason(stockOutDTO.getReason());
        stockOut.setRemark(stockOutDTO.getRemark());

        stockOutMapper.insert(stockOut);

        // 记录操作历史
        recordHistory(stockOut.getId(), "CREATE", currentUserId, operator != null ? operator.getRealName() : "-", "新建出库单");

        // 更新库存（减少）
        boolean updateSuccess = materialMapper.updateStock(stockOutDTO.getMaterialId(), -stockOutDTO.getQuantity()) > 0;
        if (!updateSuccess) {
            throw new RuntimeException("库存更新失败");
        }

        // 自动更新库存盘点记录
        inventoryCheckService.updateCheckOnStockChange(stockOutDTO.getMaterialId(), oldStock, oldStock - stockOutDTO.getQuantity());

        return convertToVO(stockOut);
    }

    @Override
    public StockOutVO getStockOutById(Long stockOutId) {
        StockOut stockOut = stockOutMapper.selectById(stockOutId);
        if (stockOut == null) {
            return null;
        }
        return convertToVO(stockOut);
    }

    @Override
    public StockOutVO getStockOutByNo(String stockOutNo) {
        StockOut stockOut = stockOutMapper.selectByStockOutNo(stockOutNo);
        if (stockOut == null) {
            return null;
        }
        return convertToVO(stockOut);
    }

    @Override
    public List<StockOutVO> getAllStockOuts() {
        List<StockOut> stockOuts = stockOutMapper.selectList(null);
        return stockOuts.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockOutVO> getStockOutsByMaterialId(Long materialId) {
        List<StockOut> stockOuts = stockOutMapper.selectByMaterialId(materialId);
        return stockOuts.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockOutVO> getStockOutsByType(String type) {
        List<StockOut> stockOuts = stockOutMapper.selectByType(type);
        return stockOuts.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public StockOutVO getStockOutByApplicationId(Long applicationId) {
        StockOut stockOut = stockOutMapper.selectByApplicationId(applicationId);
        if (stockOut == null) {
            return null;
        }
        return convertToVO(stockOut);
    }

    @Override
    public List<StockOutVO> getStockOutsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        List<StockOut> stockOuts = stockOutMapper.selectByTimeRange(startTime, endTime);
        return stockOuts.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockOutVO> getRecentStockOuts(Integer limit) {
        List<StockOut> stockOuts = stockOutMapper.selectRecent(limit);
        return stockOuts.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public Integer sumQuantityByMaterialId(Long materialId) {
        return stockOutMapper.sumQuantityByMaterialId(materialId);
    }

    @Override
    @Transactional
    public boolean deleteStockOut(Long stockOutId) {
        StockOut stockOut = stockOutMapper.selectById(stockOutId);
        if (stockOut == null) {
            throw new RuntimeException("出库单不存在");
        }

        // 获取删除前的库存（用于盘点）
        Material material = materialMapper.selectById(stockOut.getMaterialId());
        Integer oldStock = material != null ? material.getCurrentStock() : 0;

        // 还原库存（加上出库数量）
        materialMapper.updateStock(stockOut.getMaterialId(), stockOut.getQuantity());

        // 自动更新库存盘点记录
        inventoryCheckService.updateCheckOnStockChange(stockOut.getMaterialId(), oldStock, oldStock + stockOut.getQuantity());

        return stockOutMapper.deleteById(stockOutId) > 0;
    }

    @Override
    @Transactional
    public StockOutVO updateStockOut(Long stockOutId, StockOutDTO stockOutDTO) {
        StockOut stockOut = stockOutMapper.selectById(stockOutId);
        if (stockOut == null) {
            throw new RuntimeException("出库单不存在");
        }

        // 获取当前用户
        Long currentUserId = getCurrentUserId();
        User operator = userMapper.selectById(currentUserId);

        // 如果修改了物资或数量，需要调整库存
        Integer oldQuantity = stockOut.getQuantity();
        Long oldMaterialId = stockOut.getMaterialId();
        Integer newQuantity = stockOutDTO.getQuantity();
        Long newMaterialId = stockOutDTO.getMaterialId();

        // 获取修改前的库存（用于盘点）
        Material oldMaterial = materialMapper.selectById(oldMaterialId);
        Integer oldStock = oldMaterial != null ? oldMaterial.getCurrentStock() : 0;

        // 还原旧库存
        materialMapper.updateStock(oldMaterialId, oldQuantity);

        // 检查新库存是否充足
        Material material = materialMapper.selectById(newMaterialId);
        if (material == null) {
            throw new RuntimeException("物资不存在");
        }
        if (material.getCurrentStock() < newQuantity) {
            throw new RuntimeException("库存不足");
        }

        // 减少新库存
        materialMapper.updateStock(newMaterialId, -newQuantity);

        // 更新出库单
        stockOut.setMaterialId(newMaterialId);
        stockOut.setQuantity(newQuantity);
        stockOut.setType(stockOutDTO.getType());
        stockOut.setReason(stockOutDTO.getReason());
        stockOut.setRemark(stockOutDTO.getRemark());

        stockOutMapper.updateById(stockOut);

        // 记录操作历史
        String remark = "修改出库单";
        if (!oldMaterialId.equals(newMaterialId)) {
            remark += "，物资由[" + oldMaterial.getName() + "]改为[" + material.getName() + "]";
        }
        if (!oldQuantity.equals(newQuantity)) {
            remark += "，数量由[" + oldQuantity + "]改为[" + newQuantity + "]";
        }
        recordHistory(stockOut.getId(), "UPDATE", currentUserId, operator != null ? operator.getRealName() : "-", remark);

        // 自动更新库存盘点记录（原物资）
        inventoryCheckService.updateCheckOnStockChange(oldMaterialId, oldStock, oldStock + oldQuantity);

        // 如果物资也换了，也更新新物资的盘点
        if (!oldMaterialId.equals(newMaterialId)) {
            Integer newOldStock = material.getCurrentStock() + newQuantity;
            inventoryCheckService.updateCheckOnStockChange(newMaterialId, newOldStock, material.getCurrentStock());
        }

        return convertToVO(stockOut);
    }

    private String generateStockOutNo() {
        // 格式: OUT + 年月日时分秒 + 随机数
        String timePart = LocalDateTime.now().format(NO_FORMATTER);
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return "OUT" + timePart + randomPart;
    }

    private Long getCurrentUserId() {
        // 简化处理：从JWT token或SecurityContext获取
        // 这里暂时返回1（管理员ID）
        return 1L;
    }

    private void recordHistory(Long stockOutId, String action, Long operatorId, String operatorName, String remark) {
        StockOutHistory history = new StockOutHistory();
        history.setStockOutId(stockOutId);
        history.setAction(action);
        history.setOperatorId(operatorId);
        history.setOperatorName(operatorName);
        history.setRemark(remark);
        history.setCreateTime(LocalDateTime.now());
        stockOutHistoryMapper.insert(history);
    }

    private StockOutVO convertToVO(StockOut stockOut) {
        StockOutVO vo = new StockOutVO();
        BeanUtils.copyProperties(stockOut, vo);

        // 查询关联的物资信息
        Material material = materialMapper.selectById(stockOut.getMaterialId());
        if (material != null) {
            vo.setMaterialName(material.getName());
            vo.setMaterialCode(material.getCode());
            vo.setMaterialUnit(material.getUnit());
        }

        // 查询操作人信息
        if (stockOut.getOperatorId() != null) {
            User operator = userMapper.selectById(stockOut.getOperatorId());
            if (operator != null) {
                vo.setOperatorName(operator.getRealName());
            }
        }

        // 查询关联的申领单信息
        if (stockOut.getApplicationId() != null) {
            Application application = applicationMapper.selectById(stockOut.getApplicationId());
            if (application != null) {
                vo.setApplicationNo(application.getApplicationNo());
            }
        }

        // 查询操作历史
        List<StockOutHistory> histories = stockOutHistoryMapper.selectByStockOutId(stockOut.getId());
        List<StockOutHistoryVO> historyVOList = histories.stream().map(this::convertToHistoryVO).collect(Collectors.toList());
        vo.setHistoryList(historyVOList);

        return vo;
    }

    private StockOutHistoryVO convertToHistoryVO(StockOutHistory history) {
        StockOutHistoryVO vo = new StockOutHistoryVO();
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