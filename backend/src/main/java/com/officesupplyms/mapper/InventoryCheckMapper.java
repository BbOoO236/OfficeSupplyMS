package com.officesupplyms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.officesupplyms.model.entity.InventoryCheck;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存盘点Mapper接口
 */
@Mapper
public interface InventoryCheckMapper extends BaseMapper<InventoryCheck> {

    /**
     * 根据盘点单号查询
     */
    @Select("SELECT * FROM inventory_check WHERE check_no = #{checkNo}")
    InventoryCheck selectByCheckNo(@Param("checkNo") String checkNo);

    /**
     * 根据物资ID查询盘点记录
     */
    @Select("SELECT * FROM inventory_check WHERE material_id = #{materialId} ORDER BY check_time DESC")
    List<InventoryCheck> selectByMaterialId(@Param("materialId") Long materialId);

    /**
     * 根据时间范围查询盘点记录
     */
    @Select("SELECT * FROM inventory_check WHERE check_time BETWEEN #{startTime} AND #{endTime} ORDER BY check_time DESC")
    List<InventoryCheck> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 查询未调整的盘点记录
     */
    @Select("SELECT * FROM inventory_check WHERE adjustment = 0 ORDER BY check_time DESC")
    List<InventoryCheck> selectUnadjusted();

    /**
     * 查询有差异的盘点记录
     */
    @Select("SELECT * FROM inventory_check WHERE difference != 0 ORDER BY ABS(difference) DESC")
    List<InventoryCheck> selectWithDifference();

    /**
     * 统计指定物资的盘点差异总量
     */
    @Select("SELECT COALESCE(SUM(difference), 0) FROM inventory_check WHERE material_id = #{materialId}")
    Integer sumDifferenceByMaterialId(@Param("materialId") Long materialId);

    /**
     * 查询最近的盘点记录
     */
    @Select("SELECT * FROM inventory_check ORDER BY check_time DESC LIMIT #{limit}")
    List<InventoryCheck> selectRecent(@Param("limit") Integer limit);
}