package com.officesupplyms.service.impl;

import com.officesupplyms.mapper.MaterialMapper;
import com.officesupplyms.mapper.StockInMapper;
import com.officesupplyms.mapper.UserMapper;
import com.officesupplyms.model.dto.StockInDTO;
import com.officesupplyms.model.entity.Material;
import com.officesupplyms.model.entity.StockIn;
import com.officesupplyms.model.entity.User;
import com.officesupplyms.model.vo.StockInVO;
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
    private MaterialMapper materialMapper;

    @Autowired
    private UserMapper userMapper;

    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    @Transactional
    public StockInVO createStockIn(StockInDTO stockInDTO) {
        // 验证物资是否存在
        Material material = materialMapper.selectById(stockInDTO.getMaterialId());
        if (material == null) {
            throw new RuntimeException("物资不存在");
        }

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

        // 更新库存
        boolean updateSuccess = materialMapper.updateStock(stockInDTO.getMaterialId(), stockInDTO.getQuantity()) > 0;
        if (!updateSuccess) {
            throw new RuntimeException("库存更新失败");
        }

        // 更新物资单价（加权平均）
        updateMaterialUnitPrice(material, stockInDTO.getQuantity(), stockInDTO.getUnitPrice());

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

    private String generateStockInNo() {
        // 格式: IN + 年月日时分秒 + 随机数
        String timePart = LocalDateTime.now().format(NO_FORMATTER);
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return "IN" + timePart + randomPart;
    }

    private void updateMaterialUnitPrice(Material material, Integer quantity, BigDecimal newUnitPrice) {
        // 加权平均计算新单价
        BigDecimal currentStock = BigDecimal.valueOf(material.getCurrentStock() - quantity); // 入库前的库存
        BigDecimal currentTotal = material.getUnitPrice().multiply(currentStock);
        BigDecimal newTotal = newUnitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal totalStock = currentStock.add(BigDecimal.valueOf(quantity));

        if (totalStock.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal weightedPrice = currentTotal.add(newTotal).divide(totalStock, 2, BigDecimal.ROUND_HALF_UP);
            material.setUnitPrice(weightedPrice);
            materialMapper.updateById(material);
        }
    }

    private Long getCurrentUserId() {
        // 简化处理：从JWT token或SecurityContext获取
        // 这里暂时返回1（管理员ID）
        return 1L;
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

        return vo;
    }
}