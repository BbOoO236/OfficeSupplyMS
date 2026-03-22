package com.officesupplyms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.officesupplyms.model.entity.Material;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 物资Mapper接口
 */
@Mapper
public interface MaterialMapper extends BaseMapper<Material> {

    /**
     * 根据物资编码查询
     */
    @Select("SELECT * FROM material WHERE code = #{code} AND status = 1")
    Material selectByCode(@Param("code") String code);

    /**
     * 根据类别查询物资
     */
    @Select("SELECT * FROM material WHERE category = #{category} AND status = 1 ORDER BY create_time DESC")
    List<Material> selectByCategory(@Param("category") String category);

    /**
     * 根据ABC分类查询物资
     */
    @Select("SELECT * FROM material WHERE abc_class = #{abcClass} AND status = 1 ORDER BY annual_consumption_amount DESC")
    List<Material> selectByAbcClass(@Param("abcClass") String abcClass);

    /**
     * 查询所有物资（包含扩展信息）
     */
    List<Material> selectAllMaterials();

    /**
     * 更新库存
     */
    @Update("UPDATE material SET current_stock = current_stock + #{quantity}, update_time = NOW() WHERE id = #{materialId}")
    int updateStock(@Param("materialId") Long materialId, @Param("quantity") Integer quantity);

    /**
     * 更新ABC分类
     */
    @Update("UPDATE material SET abc_class = #{abcClass}, update_time = NOW() WHERE id = #{materialId}")
    int updateAbcClass(@Param("materialId") Long materialId, @Param("abcClass") String abcClass);

    /**
     * 更新年消耗数据
     */
    @Update("UPDATE material SET annual_consumption = #{annualConsumption}, annual_consumption_amount = #{annualConsumptionAmount}, update_time = NOW() WHERE id = #{materialId}")
    int updateAnnualConsumption(@Param("materialId") Long materialId,
                               @Param("annualConsumption") Integer annualConsumption,
                               @Param("annualConsumptionAmount") java.math.BigDecimal annualConsumptionAmount);

    /**
     * 查询库存低于安全库存的物资
     */
    @Select("SELECT * FROM material WHERE current_stock <= min_stock AND status = 1 ORDER BY current_stock ASC")
    List<Material> selectLowStockMaterials();

    /**
     * 查询需要补货的物资（当前库存 <= 补货点）
     */
    List<Material> selectNeedReorderMaterials();
}