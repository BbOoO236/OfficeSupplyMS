package com.officesupplyms.service.impl;

import com.officesupplyms.mapper.ConsumptionHistoryMapper;
import com.officesupplyms.model.entity.ConsumptionHistory;
import com.officesupplyms.model.vo.ConsumptionHistoryVO;
import com.officesupplyms.service.ConsumptionHistoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 历史消耗记录服务实现类
 */
@Service
public class ConsumptionHistoryServiceImpl implements ConsumptionHistoryService {

    @Autowired
    private ConsumptionHistoryMapper consumptionHistoryMapper;

    @Override
    public ConsumptionHistoryVO getById(Long id) {
        ConsumptionHistory entity = consumptionHistoryMapper.selectById(id);
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    public List<ConsumptionHistoryVO> getAll() {
        List<ConsumptionHistory> entities = consumptionHistoryMapper.selectList(null);
        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConsumptionHistoryVO> getByMaterialId(Long materialId) {
        List<ConsumptionHistory> entities = consumptionHistoryMapper.selectByMaterialId(materialId);
        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConsumptionHistoryVO> getByUserId(Long userId) {
        List<ConsumptionHistory> entities = consumptionHistoryMapper.selectByUserId(userId);
        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConsumptionHistoryVO> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        List<ConsumptionHistory> entities = consumptionHistoryMapper.selectByTimeRange(startTime, endTime);
        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConsumptionHistoryVO> getByRecordDate(LocalDate recordDate) {
        List<ConsumptionHistory> entities = consumptionHistoryMapper.selectByRecordDate(recordDate);
        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConsumptionHistoryVO> getByType(String type) {
        List<ConsumptionHistory> entities = consumptionHistoryMapper.selectByType(type);
        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConsumptionHistoryVO> getRecent(Integer limit) {
        List<ConsumptionHistory> entities = consumptionHistoryMapper.selectRecent(limit);
        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public Integer sumQuantityByMaterialId(Long materialId) {
        return consumptionHistoryMapper.sumQuantityByMaterialId(materialId);
    }

    @Override
    public Integer sumQuantityByUserId(Long userId) {
        return consumptionHistoryMapper.sumQuantityByUserId(userId);
    }

    @Override
    public boolean createConsumptionHistory(Long materialId, Long userId, Integer quantity,
                                            String type, Long applicationId, Long stockOutId) {
        ConsumptionHistory entity = new ConsumptionHistory();
        entity.setMaterialId(materialId);
        entity.setUserId(userId);
        entity.setQuantity(quantity);
        entity.setType(type);
        entity.setRecordDate(LocalDate.now());
        entity.setRecordTime(LocalDateTime.now());
        entity.setApplicationId(applicationId);
        entity.setStockOutId(stockOutId);

        int rows = consumptionHistoryMapper.insert(entity);
        return rows > 0;
    }

    private ConsumptionHistoryVO convertToVO(ConsumptionHistory entity) {
        ConsumptionHistoryVO vo = new ConsumptionHistoryVO();
        BeanUtils.copyProperties(entity, vo);

        // 这里可以补充关联信息，比如物资名称、用户姓名等
        // 暂时留空，后续可以通过关联查询填充

        return vo;
    }
}