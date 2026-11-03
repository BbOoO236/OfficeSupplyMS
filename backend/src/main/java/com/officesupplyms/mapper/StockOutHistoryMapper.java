package com.officesupplyms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.officesupplyms.model.entity.StockOutHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 出库单操作历史Mapper
 */
@Mapper
public interface StockOutHistoryMapper extends BaseMapper<StockOutHistory> {

    /**
     * 根据出库单ID查询操作历史
     */
    @Select("SELECT * FROM stock_out_history WHERE stock_out_id = #{stockOutId} ORDER BY create_time ASC")
    List<StockOutHistory> selectByStockOutId(@Param("stockOutId") Long stockOutId);
}
