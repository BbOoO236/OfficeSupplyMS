package com.officesupplyms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.officesupplyms.mapper.ApplicationHistoryMapper;
import com.officesupplyms.mapper.ApplicationMapper;
import com.officesupplyms.mapper.ConsumptionHistoryMapper;
import com.officesupplyms.mapper.MaterialMapper;
import com.officesupplyms.mapper.StockOutMapper;
import com.officesupplyms.mapper.UserMapper;
import com.officesupplyms.model.dto.ApplicationDTO;
import com.officesupplyms.model.dto.ApproveDTO;
import com.officesupplyms.model.entity.Application;
import com.officesupplyms.model.entity.ApplicationHistory;
import com.officesupplyms.model.entity.ConsumptionHistory;
import com.officesupplyms.model.entity.Material;
import com.officesupplyms.model.entity.StockOut;
import com.officesupplyms.model.entity.User;
import com.officesupplyms.model.vo.ApplicationHistoryVO;
import com.officesupplyms.model.vo.ApplicationVO;
import com.officesupplyms.model.vo.PageResult;
import com.officesupplyms.service.ApplicationService;
import com.officesupplyms.utils.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
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
    private ApplicationHistoryMapper applicationHistoryMapper;

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StockOutMapper stockOutMapper;

    @Autowired
    private ConsumptionHistoryMapper consumptionHistoryMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter STOCK_OUT_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

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

        // 记录操作历史
        recordHistory(application.getId(), "SUBMIT", null, "PENDING", application.getRemark());

        // 返回VO
        return convertToVO(application);
    }

    @Override
    @Transactional
    public ApplicationVO updateApplication(Long applicationId, ApplicationDTO applicationDTO) {
        Application application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new RuntimeException("申领单不存在");
        }

        // 只能编辑待审批状态的申领单
        if (!"PENDING".equals(application.getStatus())) {
            throw new RuntimeException("只能编辑待审批状态的申领单");
        }

        // 检查是否有权限编辑（申请人或管理员）
        Long currentUserId = getCurrentUserId();
        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new RuntimeException("用户不存在");
        }

        boolean isAdmin = "admin".equals(currentUser.getRole());
        boolean isOwner = application.getUserId().equals(currentUserId);

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("无权限编辑此申领单");
        }

        // 更新字段
        if (applicationDTO.getQuantity() != null) {
            application.setQuantity(applicationDTO.getQuantity());
        }
        if (applicationDTO.getPurpose() != null) {
            application.setPurpose(applicationDTO.getPurpose());
        }
        if (applicationDTO.getRemark() != null) {
            application.setRemark(applicationDTO.getRemark());
        }

        applicationMapper.updateById(application);

        // 记录操作历史
        recordHistory(applicationId, "UPDATE", "PENDING", "PENDING", "修改申领单");

        return convertToVO(application);
    }

    @Override
    @Transactional
    public boolean cancelApplication(Long applicationId) {
        Application application = applicationMapper.selectById(applicationId);
        if (application == null) {
            return false;
        }

        // 只能撤销待审批状态的申领单
        if (!"PENDING".equals(application.getStatus())) {
            throw new RuntimeException("只能撤销待审批状态的申领单");
        }

        // 检查是否有权限撤销（申请人或管理员）
        Long currentUserId = getCurrentUserId();
        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null) {
            return false;
        }

        boolean isAdmin = "admin".equals(currentUser.getRole());
        boolean isOwner = application.getUserId().equals(currentUserId);

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("无权限撤销此申领单");
        }

        application.setStatus("CANCELLED");
        boolean result = applicationMapper.updateById(application) > 0;

        // 记录操作历史
        if (result) {
            recordHistory(applicationId, "CANCEL", "PENDING", "CANCELLED", "用户撤销申请");
        }

        return result;
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
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            throw new RuntimeException("无审批权限");
        }

        if ("APPROVE".equals(approveDTO.getAction())) {
            // 审批通过，直接设置为待出库
            application.setStatus("TO_BE_OUTBOUND");
            application.setApproveTime(LocalDateTime.now());
            application.setApproveUserId(currentUserId);
            application.setRejectReason(null);
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

        // 记录操作历史
        String action = "APPROVE".equals(approveDTO.getAction()) ? "APPROVE" : "REJECT";
        String toStatus = "APPROVE".equals(approveDTO.getAction()) ? "TO_BE_OUTBOUND" : "REJECTED";
        String remark = approveDTO.getRejectReason() != null ? approveDTO.getRejectReason() : "";
        recordHistory(application.getId(), action, "PENDING", toStatus, remark);

        return convertToVO(application);
    }

    @Override
    @Transactional
    public boolean rollbackApplication(Long applicationId, String toStatus) {
        Application application = applicationMapper.selectById(applicationId);
        if (application == null) {
            return false;
        }

        Long currentUserId = getCurrentUserId();
        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            throw new RuntimeException("无权限回退状态");
        }

        // 状态回退逻辑
        String currentStatus = application.getStatus();

        // 已出库的物资不能回退状态
        if ("OUTBOUND".equals(currentStatus)) {
            throw new RuntimeException("已出库的物资不能回退状态");
        }

        // 状态回退只能是管理员操作
        String fromStatus = application.getStatus();
        application.setStatus(toStatus);
        boolean result = applicationMapper.updateById(application) > 0;

        // 记录操作历史
        if (result) {
            recordHistory(applicationId, "ROLLBACK", fromStatus, toStatus, "管理员回退状态");
        }

        return result;
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

        // 创建出库单记录
        StockOut stockOut = new StockOut();
        stockOut.setStockOutNo(generateStockOutNo());
        stockOut.setMaterialId(application.getMaterialId());
        stockOut.setQuantity(application.getQuantity());
        stockOut.setType("APPLICATION");
        stockOut.setApplicationId(applicationId);
        stockOut.setOutTime(LocalDateTime.now());
        stockOut.setOperatorId(currentUserId);
        stockOut.setReason(application.getPurpose());
        stockOutMapper.insert(stockOut);

        // 更新申领单状态
        application.setStatus("OUTBOUND");
        application.setOutboundTime(LocalDateTime.now());
        application.setOutboundUserId(currentUserId);
        applicationMapper.updateById(application);

        // 记录操作历史
        recordHistory(application.getId(), "OUTBOUND", "TO_BE_OUTBOUND", "OUTBOUND", "物资已出库");

        // 记录消耗历史
        ConsumptionHistory consumptionHistory = new ConsumptionHistory();
        consumptionHistory.setMaterialId(application.getMaterialId());
        consumptionHistory.setUserId(application.getUserId());
        consumptionHistory.setQuantity(application.getQuantity());
        consumptionHistory.setType("APPLICATION");
        consumptionHistory.setRecordDate(LocalDate.now());
        consumptionHistory.setRecordTime(LocalDateTime.now());
        consumptionHistory.setApplicationId(application.getId());
        consumptionHistory.setStockOutId(stockOut.getId());
        consumptionHistoryMapper.insert(consumptionHistory);

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
    public PageResult<ApplicationVO> getMyApplications(String applicationNo, String materialName, String status, Long page, Long limit) {
        Long currentUserId = getCurrentUserId();
        QueryWrapper<Application> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUserId);
        queryWrapper.orderByDesc("apply_time");

        if (applicationNo != null && !applicationNo.isEmpty()) {
            queryWrapper.like("application_no", applicationNo);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }

        List<Application> applications = applicationMapper.selectList(queryWrapper);

        // 如果有物资名称过滤，在内存中过滤（因为需要关联查询）
        if (materialName != null && !materialName.isEmpty()) {
            final String searchName = materialName;
            applications = applications.stream()
                    .filter(app -> {
                        Material material = materialMapper.selectById(app.getMaterialId());
                        return material != null && material.getName().contains(searchName);
                    })
                    .collect(Collectors.toList());
        }

        // 记录总数
        long total = applications.size();

        // 应用分页
        if (page != null && limit != null && limit > 0) {
            int start = (int) ((page - 1) * limit);
            int end = (int) Math.min(start + limit, applications.size());
            if (start < applications.size()) {
                applications = applications.subList(start, end);
            } else {
                applications = new java.util.ArrayList<>();
            }
        }

        List<ApplicationVO> voList = applications.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, total);
    }

    @Override
    public PageResult<ApplicationVO> getAllApplications(String applicationNo, String materialName, String userName, String status, Long page, Long limit) {
        QueryWrapper<Application> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("apply_time");

        if (applicationNo != null && !applicationNo.isEmpty()) {
            queryWrapper.like("application_no", applicationNo);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }

        List<Application> applications = applicationMapper.selectList(queryWrapper);

        // 物资名称和用户名称过滤在内存中进行（因为需要关联查询）
        if (materialName != null && !materialName.isEmpty() || userName != null && !userName.isEmpty()) {
            final String searchMaterialName = materialName;
            final String searchUserName = userName;
            applications = applications.stream()
                    .filter(app -> {
                        boolean match = true;
                        if (searchMaterialName != null && !searchMaterialName.isEmpty()) {
                            Material material = materialMapper.selectById(app.getMaterialId());
                            match = match && material != null && material.getName().contains(searchMaterialName);
                        }
                        if (searchUserName != null && !searchUserName.isEmpty()) {
                            User user = userMapper.selectById(app.getUserId());
                            match = match && user != null && user.getRealName().contains(searchUserName);
                        }
                        return match;
                    })
                    .collect(Collectors.toList());
        }

        // 记录总数
        long total = applications.size();

        // 应用分页
        if (page != null && limit != null && limit > 0) {
            int start = (int) ((page - 1) * limit);
            int end = (int) Math.min(start + limit, applications.size());
            if (start < applications.size()) {
                applications = applications.subList(start, end);
            } else {
                applications = new java.util.ArrayList<>();
            }
        }

        List<ApplicationVO> voList = applications.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, total);
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
        // 获取该物资的所有历史申领记录（按物资维度检测异常）
        List<Application> history = applicationMapper.selectMaterialHistory(materialId);
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

    @Override
    public List<ApplicationVO> getByUserAndMaterial(Long userId, Long materialId) {
        List<Application> applications = applicationMapper.selectUserMaterialHistory(userId, materialId);
        return applications.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private String generateApplicationNo() {
        // 格式: APP + 年月日时分秒 + 随机数
        String timePart = LocalDateTime.now().format(NO_FORMATTER);
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return "APP" + timePart + randomPart;
    }

    private String generateStockOutNo() {
        // 格式: OUT + 年月日时分秒 + 随机数
        String timePart = LocalDateTime.now().format(STOCK_OUT_NO_FORMATTER);
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return "OUT" + timePart + randomPart;
    }

    private Long getCurrentUserId() {
        // 从请求头中获取token
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("未找到有效的身份验证令牌");
        }
        String token = authHeader.substring(7);
        // 验证token
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("令牌无效或已过期");
        }
        return jwtUtil.getUserIdFromToken(token);
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
            vo.setMaterialStock(material.getCurrentStock());
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

        // 查询操作历史
        List<ApplicationHistory> histories = applicationHistoryMapper.selectByApplicationId(application.getId());
        List<ApplicationHistoryVO> historyVOList = histories.stream().map(this::convertToHistoryVO).collect(Collectors.toList());
        vo.setHistoryList(historyVOList);

        return vo;
    }

    private ApplicationHistoryVO convertToHistoryVO(ApplicationHistory history) {
        ApplicationHistoryVO vo = new ApplicationHistoryVO();
        BeanUtils.copyProperties(history, vo);
        vo.setActionText(getActionText(history.getAction()));
        return vo;
    }

    private String getActionText(String action) {
        switch (action) {
            case "SUBMIT": return "提交申请";
            case "APPROVE": return "审批通过";
            case "REJECT": return "审批拒绝";
            case "OUTBOUND": return "已出库";
            case "CANCEL": return "撤销申请";
            case "ROLLBACK": return "状态回退";
            default: return action;
        }
    }

    private void recordHistory(Long applicationId, String action, String fromStatus, String toStatus, String remark) {
        Long operatorId = getCurrentUserId();
        User operator = userMapper.selectById(operatorId);

        ApplicationHistory history = new ApplicationHistory();
        history.setApplicationId(applicationId);
        history.setAction(action);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setOperatorId(operatorId);
        history.setOperatorName(operator != null ? operator.getRealName() : null);
        history.setRemark(remark);
        history.setCreateTime(LocalDateTime.now());

        applicationHistoryMapper.insert(history);
    }
}