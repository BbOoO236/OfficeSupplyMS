package com.officesupplyms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.officesupplyms.model.entity.Application;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

/**
 * 申领单Mapper接口
 */
@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {

    /**
     * 根据申领单号查询
     */
    @Select("SELECT * FROM application WHERE application_no = #{applicationNo}")
    Application selectByApplicationNo(@Param("applicationNo") String applicationNo);

    /**
     * 根据用户ID查询申领单
     */
    @Select("SELECT * FROM application WHERE user_id = #{userId} ORDER BY apply_time DESC")
    List<Application> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据物资ID查询申领单
     */
    @Select("SELECT * FROM application WHERE material_id = #{materialId} ORDER BY apply_time DESC")
    List<Application> selectByMaterialId(@Param("materialId") Long materialId);

    /**
     * 根据状态查询申领单
     */
    @Select("SELECT * FROM application WHERE status = #{status} ORDER BY apply_time DESC")
    List<Application> selectByStatus(@Param("status") String status);

    /**
     * 查询用户对某物资的历史申领记录
     */
    @Select("SELECT * FROM application WHERE user_id = #{userId} AND material_id = #{materialId} AND status IN ('APPROVED', 'COMPLETED') ORDER BY apply_time DESC")
    List<Application> selectUserMaterialHistory(@Param("userId") Long userId, @Param("materialId") Long materialId);

    /**
     * 查询指定时间范围内的申领记录
     */
    List<Application> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各物资申领数量TOP10
     */
    List<MaterialApplicationStat> selectTop10MaterialsByApplication();

    /**
     * 统计每月申领单数量与金额
     */
    List<MonthlyApplicationStat> selectMonthlyApplicationStats();

    class MaterialApplicationStat {
        private Long materialId;
        private String materialName;
        private Integer totalQuantity;
        // getter/setter
        public Long getMaterialId() { return materialId; }
        public void setMaterialId(Long materialId) { this.materialId = materialId; }
        public String getMaterialName() { return materialName; }
        public void setMaterialName(String materialName) { this.materialName = materialName; }
        public Integer getTotalQuantity() { return totalQuantity; }
        public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }
    }

    class MonthlyApplicationStat {
        private String month;
        private Integer applicationCount;
        private BigDecimal totalAmount;
        // getter/setter
        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public Integer getApplicationCount() { return applicationCount; }
        public void setApplicationCount(Integer applicationCount) { this.applicationCount = applicationCount; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }
}