package com.officesupplyms.service.impl;

import com.officesupplyms.mapper.AlertRecordMapper;
import com.officesupplyms.mapper.MaterialMapper;
import com.officesupplyms.mapper.UserMapper;
import com.officesupplyms.model.entity.AlertRecord;
import com.officesupplyms.model.entity.Material;
import com.officesupplyms.model.entity.User;
import com.officesupplyms.model.vo.AlertRecordVO;
import com.officesupplyms.service.AlertRecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 预警记录服务实现类
 */
@Service
public class AlertRecordServiceImpl implements AlertRecordService {

    @Autowired
    private AlertRecordMapper alertRecordMapper;

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public boolean createAlertRecord(Long materialId, String alertType, String alertLevel,
                                     java.math.BigDecimal currentValue, java.math.BigDecimal threshold,
                                     String message) {
        Material material = materialMapper.selectById(materialId);
        if (material == null) {
            return false;
        }

        AlertRecord alertRecord = new AlertRecord();
        alertRecord.setMaterialId(materialId);
        alertRecord.setAlertType(alertType);
        alertRecord.setAlertLevel(alertLevel);
        alertRecord.setCurrentValue(currentValue);
        alertRecord.setThreshold(threshold);
        alertRecord.setMessage(message);
        alertRecord.setStatus("UNREAD");
        alertRecord.setAlertTime(LocalDateTime.now());

        alertRecordMapper.insert(alertRecord);
        return true;
    }

    @Override
    public AlertRecordVO getAlertRecordById(Long alertId) {
        AlertRecord alertRecord = alertRecordMapper.selectById(alertId);
        if (alertRecord == null) {
            return null;
        }
        return convertToVO(alertRecord);
    }

    @Override
    public List<AlertRecordVO> getAllAlertRecords() {
        List<AlertRecord> alertRecords = alertRecordMapper.selectList(null);
        return alertRecords.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AlertRecordVO> getAlertRecordsByType(String alertType) {
        List<AlertRecord> alertRecords = alertRecordMapper.selectByAlertType(alertType);
        return alertRecords.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AlertRecordVO> getAlertRecordsByLevel(String alertLevel) {
        List<AlertRecord> alertRecords = alertRecordMapper.selectByAlertLevel(alertLevel);
        return alertRecords.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AlertRecordVO> getAlertRecordsByStatus(String status) {
        List<AlertRecord> alertRecords = alertRecordMapper.selectByStatus(status);
        return alertRecords.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AlertRecordVO> getAlertRecordsByMaterialId(Long materialId) {
        List<AlertRecord> alertRecords = alertRecordMapper.selectByMaterialId(materialId);
        return alertRecords.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AlertRecordVO> getAlertRecordsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        List<AlertRecord> alertRecords = alertRecordMapper.selectByTimeRange(startTime, endTime);
        return alertRecords.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AlertRecordVO> getUnreadAlertRecords() {
        List<AlertRecord> alertRecords = alertRecordMapper.selectUnread();
        return alertRecords.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean markAsRead(Long alertId) {
        AlertRecord alertRecord = alertRecordMapper.selectById(alertId);
        if (alertRecord == null) {
            return false;
        }

        alertRecord.setStatus("READ");
        alertRecord.setReadTime(LocalDateTime.now());
        alertRecordMapper.updateById(alertRecord);
        return true;
    }

    @Override
    @Transactional
    public boolean markAsProcessed(Long alertId, Long processUserId) {
        AlertRecord alertRecord = alertRecordMapper.selectById(alertId);
        if (alertRecord == null) {
            return false;
        }

        alertRecord.setStatus("PROCESSED");
        alertRecord.setProcessTime(LocalDateTime.now());
        alertRecord.setProcessUserId(processUserId);
        alertRecordMapper.updateById(alertRecord);
        return true;
    }

    @Override
    public Integer countUnreadAlertRecords() {
        return alertRecordMapper.countUnread();
    }

    @Override
    public List<AlertRecordVO> getRecentAlertRecords(Integer limit) {
        List<AlertRecord> alertRecords = alertRecordMapper.selectRecent(limit);
        return alertRecords.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private AlertRecordVO convertToVO(AlertRecord alertRecord) {
        AlertRecordVO vo = new AlertRecordVO();
        BeanUtils.copyProperties(alertRecord, vo);

        // 查询关联的物资信息
        Material material = materialMapper.selectById(alertRecord.getMaterialId());
        if (material != null) {
            vo.setMaterialName(material.getName());
            vo.setMaterialCode(material.getCode());
        }

        // 查询处理人信息
        if (alertRecord.getProcessUserId() != null) {
            User processUser = userMapper.selectById(alertRecord.getProcessUserId());
            if (processUser != null) {
                vo.setProcessUserName(processUser.getRealName());
            }
        }

        return vo;
    }
}