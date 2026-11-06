package com.officesupplyms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.officesupplyms.model.entity.StockInHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 入库单操作历史Mapper
 */
@Mapper
public interface StockInHistoryMapper extends BaseMapper<StockInHistory> {

    /**
     * 根据入库单ID查询操作历史
     */
    @Select("SELECT * FROM stock_in_history WHERE stock_in_id = #{stockInId} ORDER BY create_time ASC")
    List<StockInHistory> selectByStockInId(@Param("stockInId") Long stockInId);
}
