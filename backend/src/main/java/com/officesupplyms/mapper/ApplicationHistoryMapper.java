package com.officesupplyms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.officesupplyms.model.entity.ApplicationHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 申领单操作历史Mapper
 */
@Mapper
public interface ApplicationHistoryMapper extends BaseMapper<ApplicationHistory> {

    /**
     * 根据申领单ID查询操作历史
     */
    @Select("SELECT * FROM application_history WHERE application_id = #{applicationId} ORDER BY create_time ASC")
    List<ApplicationHistory> selectByApplicationId(@Param("applicationId") Long applicationId);
}
