package com.officesupplyms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.officesupplyms.model.entity.AlertRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预警记录Mapper接口
 */
@Mapper
public interface AlertRecordMapper extends BaseMapper<AlertRecord> {

    /**
     * 根据预警类型查询
     */
    @Select("SELECT * FROM alert_record WHERE alert_type = #{alertType} ORDER BY alert_time DESC")
    List<AlertRecord> selectByAlertType(@Param("alertType") String alertType);

    /**
     * 根据预警等级查询
     */
    @Select("SELECT * FROM alert_record WHERE alert_level = #{alertLevel} ORDER BY alert_time DESC")
    List<AlertRecord> selectByAlertLevel(@Param("alertLevel") String alertLevel);

    /**
     * 根据状态查询预警记录
     */
    @Select("SELECT * FROM alert_record WHERE status = #{status} ORDER BY alert_time DESC")
    List<AlertRecord> selectByStatus(@Param("status") String status);

    /**
     * 根据物资ID查询预警记录
     */
    @Select("SELECT * FROM alert_record WHERE material_id = #{materialId} ORDER BY alert_time DESC")
    List<AlertRecord> selectByMaterialId(@Param("materialId") Long materialId);

    /**
     * 根据时间范围查询预警记录
     */
    @Select("SELECT * FROM alert_record WHERE alert_time BETWEEN #{startTime} AND #{endTime} ORDER BY alert_time DESC")
    List<AlertRecord> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    /**
     * 查询未读预警记录
     */
    @Select("SELECT * FROM alert_record WHERE status = 'UNREAD' ORDER BY alert_time DESC")
    List<AlertRecord> selectUnread();

    /**
     * 统计未读预警数量
     */
    @Select("SELECT COUNT(*) FROM alert_record WHERE status = 'UNREAD'")
    Integer countUnread();

    /**
     * 查询最近的预警记录
     */
    @Select("SELECT * FROM alert_record ORDER BY alert_time DESC LIMIT #{limit}")
    List<AlertRecord> selectRecent(@Param("limit") Integer limit);
}