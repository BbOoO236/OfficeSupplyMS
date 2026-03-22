package com.officesupplyms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.officesupplyms.model.entity.StockOut;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 出库单Mapper接口
 */
@Mapper
public interface StockOutMapper extends BaseMapper<StockOut> {

    /**
     * 根据出库单号查询
     */
    @Select("SELECT * FROM stock_out WHERE stock_out_no = #{stockOutNo}")
    StockOut selectByStockOutNo(@Param("stockOutNo") String stockOutNo);

    /**
     * 根据物资ID查询出库记录
     */
    @Select("SELECT * FROM stock_out WHERE material_id = #{materialId} ORDER BY out_time DESC")
    List<StockOut> selectByMaterialId(@Param("materialId") Long materialId);

    /**
     * 根据申领单ID查询出库记录
     */
    @Select("SELECT * FROM stock_out WHERE application_id = #{applicationId}")
    StockOut selectByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 根据出库类型查询出库记录
     */
    @Select("SELECT * FROM stock_out WHERE type = #{type} ORDER BY out_time DESC")
    List<StockOut> selectByType(@Param("type") String type);

    /**
     * 根据时间范围查询出库记录
     */
    @Select("SELECT * FROM stock_out WHERE out_time BETWEEN #{startTime} AND #{endTime} ORDER BY out_time DESC")
    List<StockOut> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定物资的总出库数量
     */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM stock_out WHERE material_id = #{materialId}")
    Integer sumQuantityByMaterialId(@Param("materialId") Long materialId);

    /**
     * 查询最近的出库记录
     */
    @Select("SELECT * FROM stock_out ORDER BY out_time DESC LIMIT #{limit}")
    List<StockOut> selectRecent(@Param("limit") Integer limit);
}