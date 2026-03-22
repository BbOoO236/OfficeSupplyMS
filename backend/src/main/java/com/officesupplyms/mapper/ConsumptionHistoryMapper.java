package com.officesupplyms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.officesupplyms.model.entity.ConsumptionHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 历史消耗记录Mapper接口
 */
@Mapper
public interface ConsumptionHistoryMapper extends BaseMapper<ConsumptionHistory> {

    /**
     * 根据物资ID和日期范围查询消耗记录
     */
    @Select("SELECT * FROM consumption_history WHERE material_id = #{materialId} AND record_date BETWEEN #{startDate} AND #{endDate} ORDER BY record_date")
    List<ConsumptionHistory> selectByMaterialAndDateRange(@Param("materialId") Long materialId,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);

    /**
     * 根据用户ID和物资ID查询消耗记录
     */
    @Select("SELECT * FROM consumption_history WHERE user_id = #{userId} AND material_id = #{materialId} ORDER BY record_date DESC")
    List<ConsumptionHistory> selectByUserAndMaterial(@Param("userId") Long userId,
                                                    @Param("materialId") Long materialId);

    /**
     * 计算物资的日均消耗量
     */
    @Select("SELECT AVG(quantity) as avg_daily_consumption FROM (" +
            "SELECT SUM(quantity) as quantity FROM consumption_history " +
            "WHERE material_id = #{materialId} AND record_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY record_date" +
            ") as daily_sum")
    Double calculateAverageDailyConsumption(@Param("materialId") Long materialId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * 统计物资的月消耗量
     */
    @Select("SELECT DATE_FORMAT(record_date, '%Y-%m') as month, SUM(quantity) as totalQuantity " +
            "FROM consumption_history " +
            "WHERE material_id = #{materialId} " +
            "AND record_date >= DATE_SUB(CURDATE(), INTERVAL #{months} MONTH) " +
            "GROUP BY DATE_FORMAT(record_date, '%Y-%m') " +
            "ORDER BY month")
    @ResultType(MonthlyConsumption.class)
    List<MonthlyConsumption> selectMonthlyConsumption(@Param("materialId") Long materialId,
                                                     @Param("months") Integer months);

    /**
     * 获取用户对某物资的历史申领统计数据（用于异常检测）
     */
    @Select("SELECT AVG(quantity) as mean, STDDEV_SAMP(quantity) as std_dev " +
            "FROM consumption_history " +
            "WHERE user_id = #{userId} AND material_id = #{materialId} AND type = 'APPLICATION'")
    UserMaterialStat selectUserMaterialStatistics(@Param("userId") Long userId,
                                                 @Param("materialId") Long materialId);

    /**
     * 根据物资ID查询消耗记录
     */
    @Select("SELECT * FROM consumption_history WHERE material_id = #{materialId} ORDER BY record_time DESC")
    List<ConsumptionHistory> selectByMaterialId(@Param("materialId") Long materialId);

    /**
     * 根据用户ID查询消耗记录
     */
    @Select("SELECT * FROM consumption_history WHERE user_id = #{userId} ORDER BY record_time DESC")
    List<ConsumptionHistory> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据时间范围查询消耗记录
     */
    @Select("SELECT * FROM consumption_history WHERE record_time BETWEEN #{startTime} AND #{endTime} ORDER BY record_time DESC")
    List<ConsumptionHistory> selectByTimeRange(@Param("startTime") java.time.LocalDateTime startTime,
                                               @Param("endTime") java.time.LocalDateTime endTime);

    /**
     * 根据记录日期查询消耗记录
     */
    @Select("SELECT * FROM consumption_history WHERE record_date = #{recordDate} ORDER BY record_time DESC")
    List<ConsumptionHistory> selectByRecordDate(@Param("recordDate") LocalDate recordDate);

    /**
     * 根据类型查询消耗记录
     */
    @Select("SELECT * FROM consumption_history WHERE type = #{type} ORDER BY record_time DESC")
    List<ConsumptionHistory> selectByType(@Param("type") String type);

    /**
     * 查询最近的消耗记录
     */
    @Select("SELECT * FROM consumption_history ORDER BY record_time DESC LIMIT #{limit}")
    List<ConsumptionHistory> selectRecent(@Param("limit") Integer limit);

    /**
     * 统计指定物资的总消耗数量
     */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM consumption_history WHERE material_id = #{materialId}")
    Integer sumQuantityByMaterialId(@Param("materialId") Long materialId);

    /**
     * 统计指定物资在指定时间范围内的总消耗数量
     */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM consumption_history WHERE material_id = #{materialId} AND record_date BETWEEN #{startDate} AND #{endDate}")
    Integer sumQuantityByMaterialIdAndDateRange(@Param("materialId") Long materialId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * 统计指定用户的总消耗数量
     */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM consumption_history WHERE user_id = #{userId}")
    Integer sumQuantityByUserId(@Param("userId") Long userId);

    class MonthlyConsumption {
        private String month;
        private Integer totalQuantity;

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public Integer getTotalQuantity() { return totalQuantity; }
        public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }
    }

    class UserMaterialStat {
        private Double mean;
        private Double stdDev;

        public Double getMean() { return mean; }
        public void setMean(Double mean) { this.mean = mean; }
        public Double getStdDev() { return stdDev; }
        public void setStdDev(Double stdDev) { this.stdDev = stdDev; }
    }
}