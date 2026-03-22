package com.officesupplyms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.officesupplyms.mapper.ApplicationMapper;
import com.officesupplyms.mapper.MaterialMapper;
import com.officesupplyms.mapper.UserMapper;
import com.officesupplyms.model.dto.ApplicationDTO;
import com.officesupplyms.model.dto.ApproveDTO;
import com.officesupplyms.model.entity.Application;
import com.officesupplyms.model.entity.Material;
import com.officesupplyms.model.entity.User;
import com.officesupplyms.model.vo.ApplicationVO;
import com.officesupplyms.service.ApplicationService;
import com.officesupplyms.utils.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 申领服务实现类
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    @Transactional
    public ApplicationVO createApplication(ApplicationDTO applicationDTO) {
        // 验证物资是否存在
        Material material = materialMapper.selectById(applicationDTO.getMaterialId());
        if (material == null) {
            throw new RuntimeException("物资不存在");
        }

        // 检查库存是否充足
        if (material.getCurrentStock() < applicationDTO.getQuantity()) {
            throw new RuntimeException("库存不足");
        }

        // 获取当前用户ID（简化处理，实际应从token或SecurityContext获取）
        Long currentUserId = getCurrentUserId();
        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检测申领是否异常
        boolean isAbnormal = detectAnomaly(currentUserId, applicationDTO.getMaterialId(), applicationDTO.getQuantity());

        // 生成申领单号
        String applicationNo = generateApplicationNo();

        // 创建申领单
        Application application = new Application();
        application.setApplicationNo(applicationNo);
        application.setUserId(currentUserId);
        application.setMaterialId(applicationDTO.getMaterialId());
        application.setQuantity(applicationDTO.getQuantity());
        application.setPurpose(applicationDTO.getPurpose());
        application.setStatus("PENDING");
        application.setApplyTime(LocalDateTime.now());
        application.setIsAbnormal(isAbnormal ? 1 : 0);
        if (isAbnormal) {
            application.setAbnormalReason("申领数量超过历史平均值的3倍标准差");
        }
        application.setRemark(applicationDTO.getRemark());

        applicationMapper.insert(application);

        // 返回VO
        return convertToVO(application);
    }

    @Override
    @Transactional
    public ApplicationVO approveApplication(ApproveDTO approveDTO) {
        Application application = applicationMapper.selectById(approveDTO.getApplicationId());
        if (application == null) {
            throw new RuntimeException("申领单不存在");
        }

        if (!"PENDING".equals(application.getStatus())) {
            throw new RuntimeException("申领单状态不是待审批");
        }

        Long currentUserId = getCurrentUserId();
        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("无审批权限");
        }

        if ("APPROVE".equals(approveDTO.getAction())) {
            // 审批通过
            application.setStatus("APPROVED");
            application.setApproveTime(LocalDateTime.now());
            application.setApproveUserId(currentUserId);
            application.setRejectReason(null);

            // 更新申领单状态为待出库
            application.setStatus("TO_BE_OUTBOUND");
        } else if ("REJECT".equals(approveDTO.getAction())) {
            // 审批驳回
            application.setStatus("REJECTED");
            application.setApproveTime(LocalDateTime.now());
            application.setApproveUserId(currentUserId);
            application.setRejectReason(approveDTO.getRejectReason());
        } else {
            throw new RuntimeException("无效的审批操作");
        }

        applicationMapper.updateById(application);
        return convertToVO(application);
    }

    @Override
    @Transactional
    public ApplicationVO outboundApplication(Long applicationId) {
        Application application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new RuntimeException("申领单不存在");
        }

        if (!"TO_BE_OUTBOUND".equals(application.getStatus())) {
            throw new RuntimeException("申领单状态不是待出库");
        }

        // 获取当前用户ID（出库操作人）
        Long currentUserId = getCurrentUserId();

        // 检查库存是否充足
        Material material = materialMapper.selectById(application.getMaterialId());
        if (material == null || material.getCurrentStock() < application.getQuantity()) {
            throw new RuntimeException("库存不足");
        }

        // 出库操作：减少库存
        boolean stockOutSuccess = materialMapper.updateStock(application.getMaterialId(), -application.getQuantity()) > 0;
        if (!stockOutSuccess) {
            throw new RuntimeException("库存更新失败");
        }

        // 更新申领单状态
        application.setStatus("COMPLETED");
        application.setOutboundTime(LocalDateTime.now());
        application.setOutboundUserId(currentUserId);
        applicationMapper.updateById(application);

        // 记录消耗历史（可在此处调用ConsumptionHistoryService）

        return convertToVO(application);
    }

    @Override
    public ApplicationVO getApplicationById(Long applicationId) {
        Application application = applicationMapper.selectById(applicationId);
        if (application == null) {
            return null;
        }
        return convertToVO(application);
    }

    @Override
    public ApplicationVO getApplicationByNo(String applicationNo) {
        Application application = applicationMapper.selectByApplicationNo(applicationNo);
        if (application == null) {
            return null;
        }
        return convertToVO(application);
    }

    @Override
    public List<ApplicationVO> getMyApplications() {
        Long currentUserId = getCurrentUserId();
        List<Application> applications = applicationMapper.selectByUserId(currentUserId);
        return applications.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationVO> getAllApplications() {
        List<Application> applications = applicationMapper.selectList(null);
        return applications.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationVO> getApplicationsByStatus(String status) {
        List<Application> applications = applicationMapper.selectByStatus(status);
        return applications.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationVO> getApplicationsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        List<Application> applications = applicationMapper.selectByTimeRange(startTime, endTime);
        return applications.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationVO> getPendingApplications() {
        return getApplicationsByStatus("PENDING");
    }

    @Override
    public List<ApplicationVO> getToBeOutboundApplications() {
        return getApplicationsByStatus("TO_BE_OUTBOUND");
    }

    @Override
    public List<ApplicationVO> getAbnormalApplications() {
        QueryWrapper<Application> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_abnormal", 1);
        List<Application> applications = applicationMapper.selectList(queryWrapper);
        return applications.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getTop10MaterialsByApplication() {
        List<ApplicationMapper.MaterialApplicationStat> stats = applicationMapper.selectTop10MaterialsByApplication();
        return stats.stream().map(stat -> {
            Map<String, Object> map = new HashMap<>();
            map.put("materialId", stat.getMaterialId());
            map.put("materialName", stat.getMaterialName());
            map.put("totalQuantity", stat.getTotalQuantity());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getMonthlyApplicationStats() {
        List<ApplicationMapper.MonthlyApplicationStat> stats = applicationMapper.selectMonthlyApplicationStats();
        return stats.stream().map(stat -> {
            Map<String, Object> map = new HashMap<>();
            map.put("month", stat.getMonth());
            map.put("applicationCount", stat.getApplicationCount());
            map.put("totalAmount", stat.getTotalAmount());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean detectAnomaly(Long userId, Long materialId, Integer quantity) {
        // 获取该用户对该物资的历史申领记录
        List<Application> history = applicationMapper.selectUserMaterialHistory(userId, materialId);
        if (history.isEmpty()) {
            return false; // 无历史记录，无法检测异常
        }

        // 计算历史申领数量的平均值和标准差
        List<Integer> quantities = history.stream()
                .map(Application::getQuantity)
                .collect(Collectors.toList());

        double mean = quantities.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double variance = quantities.stream()
                .mapToDouble(q -> Math.pow(q - mean, 2))
                .average().orElse(0.0);
        double stdDev = Math.sqrt(variance);

        // 3σ原则检测异常
        double threshold = mean + 3 * stdDev;
        return quantity > threshold;
    }

    private String generateApplicationNo() {
        // 格式: APP + 年月日时分秒 + 随机数
        String timePart = LocalDateTime.now().format(NO_FORMATTER);
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return "APP" + timePart + randomPart;
    }

    private Long getCurrentUserId() {
        // 简化处理：从JWT token或SecurityContext获取
        // 这里暂时返回1（管理员ID）
        return 1L;
    }

    private ApplicationVO convertToVO(Application application) {
        ApplicationVO vo = new ApplicationVO();
        BeanUtils.copyProperties(application, vo);

        // 查询关联的用户信息
        User user = userMapper.selectById(application.getUserId());
        if (user != null) {
            vo.setUserName(user.getRealName());
            vo.setUserDepartment(user.getDepartment());
        }

        // 查询关联的物资信息
        Material material = materialMapper.selectById(application.getMaterialId());
        if (material != null) {
            vo.setMaterialName(material.getName());
            vo.setMaterialCode(material.getCode());
            vo.setMaterialUnit(material.getUnit());
        }

        // 查询审批人信息
        if (application.getApproveUserId() != null) {
            User approveUser = userMapper.selectById(application.getApproveUserId());
            if (approveUser != null) {
                vo.setApproveUserName(approveUser.getRealName());
            }
        }

        // 查询出库操作人信息
        if (application.getOutboundUserId() != null) {
            User outboundUser = userMapper.selectById(application.getOutboundUserId());
            if (outboundUser != null) {
                vo.setOutboundUserName(outboundUser.getRealName());
            }
        }

        return vo;
    }
}