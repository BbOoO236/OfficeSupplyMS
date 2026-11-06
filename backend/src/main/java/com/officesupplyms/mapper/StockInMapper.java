package com.officesupplyms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.officesupplyms.model.entity.StockIn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 入库单Mapper接口
 */
@Mapper
public interface StockInMapper extends BaseMapper<StockIn> {

    /**
     * 根据入库单号查询
     */
    @Select("SELECT * FROM stock_in WHERE stock_in_no = #{stockInNo}")
    StockIn selectByStockInNo(@Param("stockInNo") String stockInNo);

    /**
     * 根据物资ID查询入库记录
     */
    @Select("SELECT * FROM stock_in WHERE material_id = #{materialId} ORDER BY in_time DESC")
    List<StockIn> selectByMaterialId(@Param("materialId") Long materialId);

    /**
     * 根据时间范围查询入库记录
     */
    @Select("SELECT * FROM stock_in WHERE in_time BETWEEN #{startTime} AND #{endTime} ORDER BY in_time DESC")
    List<StockIn> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定物资的总入库数量
     */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM stock_in WHERE material_id = #{materialId}")
    Integer sumQuantityByMaterialId(@Param("materialId") Long materialId);

    /**
     * 统计指定物资的总入库金额
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM stock_in WHERE material_id = #{materialId}")
    java.math.BigDecimal sumAmountByMaterialId(@Param("materialId") Long materialId);

    /**
     * 查询最近的入库记录
     */
    @Select("SELECT * FROM stock_in ORDER BY in_time DESC LIMIT #{limit}")
    List<StockIn> selectRecent(@Param("limit") Integer limit);
}