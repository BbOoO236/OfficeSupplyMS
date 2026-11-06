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
     * 设置库存为指定值
     */
    @Update("UPDATE material SET current_stock = #{stock}, update_time = NOW() WHERE id = #{materialId}")
    int updateStockToValue(@Param("materialId") Long materialId, @Param("stock") Integer stock);

    /**
     * 更新ABC分类
     */
    @Update("UPDATE material SET abc_class = #{abcClass}, update_time = NOW() WHERE id = #{materialId}")
    int updateAbcClass(@Param("materialId") Long materialId, @Param("abcClass") String abcClass);

    /**
     * 只更新单价（不更新其他字段，避免覆盖库存等字段）
     */
    @Update("UPDATE material SET unit_price = #{unitPrice}, update_time = NOW() WHERE id = #{materialId}")
    int updateUnitPriceOnly(@Param("materialId") Long materialId, @Param("unitPrice") java.math.BigDecimal unitPrice);

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
     * 查询需要补货的物资（当前库存 <= 最低库存）
     */
    @Select("SELECT * FROM material WHERE current_stock <= min_stock AND status = 1 ORDER BY current_stock ASC")
    List<Material> selectNeedReorderMaterials();

    /**
     * 统计库存状态分布（正常、库存过低、库存过高）
     */
    @Select("SELECT " +
            "CAST(SUM(CASE WHEN current_stock > min_stock AND current_stock < max_stock THEN 1 ELSE 0 END) AS SIGNED) as normalCount, " +
            "CAST(SUM(CASE WHEN current_stock <= min_stock THEN 1 ELSE 0 END) AS SIGNED) as lowCount, " +
            "CAST(SUM(CASE WHEN current_stock >= max_stock THEN 1 ELSE 0 END) AS SIGNED) as highCount " +
            "FROM material WHERE status = 1")
    StockStatusStat selectStockStatusStats();

    class StockStatusStat {
        private Integer normalCount;
        private Integer lowCount;
        private Integer highCount;
        // getter/setter
        public Integer getNormalCount() { return normalCount; }
        public void setNormalCount(Integer normalCount) { this.normalCount = normalCount; }
        public Integer getLowCount() { return lowCount; }
        public void setLowCount(Integer lowCount) { this.lowCount = lowCount; }
        public Integer getHighCount() { return highCount; }
        public void setHighCount(Integer highCount) { this.highCount = highCount; }
    }
}