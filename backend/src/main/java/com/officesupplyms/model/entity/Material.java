package com.officesupplyms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物资实体类
 */
@Data
@TableName("material")
public class Material {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code; // 物资编码

    private String name; // 物资名称

    private String category; // 物资类别

    private String specification; // 规格

    private String unit; // 单位

    private BigDecimal unitPrice; // 单价

    private Integer leadTime; // 采购提前期(天)

    private BigDecimal safetyFactor; // 安全系数

    private Integer currentStock; // 当前库存

    private Integer minStock; // 最低库存

    private Integer maxStock; // 最高库存

    private String abcClass; // ABC分类: A, B, C

    private Integer annualConsumption; // 年消耗量

    private BigDecimal annualConsumptionAmount; // 年消耗金额

    private Integer status; // 1-正常, 0-停用

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}