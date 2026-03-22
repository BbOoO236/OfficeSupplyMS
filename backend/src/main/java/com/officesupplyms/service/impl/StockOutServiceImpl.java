package com.officesupplyms.service.impl;

import com.officesupplyms.mapper.ApplicationMapper;
import com.officesupplyms.mapper.MaterialMapper;
import com.officesupplyms.mapper.StockOutMapper;
import com.officesupplyms.mapper.UserMapper;
import com.officesupplyms.model.dto.StockOutDTO;
import com.officesupplyms.model.entity.Application;
import com.officesupplyms.model.entity.Material;
import com.officesupplyms.model.entity.StockOut;
import com.officesupplyms.model.entity.User;
import com.officesupplyms.model.vo.StockOutVO;
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
    private MaterialMapper materialMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ApplicationMapper applicationMapper;

    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    @Transactional
    public StockOutVO createStockOut(StockOutDTO stockOutDTO) {
        // 验证物资是否存在
        Material material = materialMapper.selectById(stockOutDTO.getMaterialId());
        if (material == null) {
            throw new RuntimeException("物资不存在");
        }

        // 检查库存是否充足
        if (material.getCurrentStock() < stockOutDTO.getQuantity()) {
            throw new RuntimeException("库存不足");
        }

        // 获取当前用户ID（操作人）
        Long currentUserId = getCurrentUserId();
        User operator = userMapper.selectById(currentUserId);
        if (operator == null) {
            throw new RuntimeException("操作人不存在");
        }

        // 如果出库类型为APPLICATION，验证申领单
        if ("APPLICATION".equals(stockOutDTO.getType())) {
            if (stockOutDTO.getApplicationId() == null) {
                throw new RuntimeException("申领出库必须关联申领单");
            }
            Application application = applicationMapper.selectById(stockOutDTO.getApplicationId());
            if (application == null) {
                throw new RuntimeException("关联的申领单不存在");
            }
            // 可进一步验证申领单状态
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

        // 更新库存（减少）
        boolean updateSuccess = materialMapper.updateStock(stockOutDTO.getMaterialId(), -stockOutDTO.getQuantity()) > 0;
        if (!updateSuccess) {
            throw new RuntimeException("库存更新失败");
        }

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
        User operator = userMapper.selectById(stockOut.getOperatorId());
        if (operator != null) {
            vo.setOperatorName(operator.getRealName());
        }

        // 查询关联的申领单信息
        if (stockOut.getApplicationId() != null) {
            Application application = applicationMapper.selectById(stockOut.getApplicationId());
            if (application != null) {
                vo.setApplicationNo(application.getApplicationNo());
            }
        }

        return vo;
    }
}