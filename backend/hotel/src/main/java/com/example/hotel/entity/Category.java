package com.example.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "category", autoResultMap = true)
public class Category {

    /** 房型id（主键），String 类型，走雪花算法由框架自动生成 */
    @TableId(type = IdType.ASSIGN_ID)
    @Size(max = 20, message = "房型id长度不能超过20个字符")
    private String id;

    /** 房型名字（如豪华大床房），建议唯一 */
    @NotBlank(message = "房型名不能为空")
    private String name;

    /** 房型基础价格，元/天 */
    @NotNull(message = "基础价格不能为空")
    @DecimalMin(value = "0", inclusive = true, message = "基础价格不能为负数")
    @TableField("basicPrice")
    private BigDecimal basicPrice;

    /** 酒店建设时该房型数量 */
    @NotNull(message = "房型数量不能为空")
    @PositiveOrZero(message = "房型数量不能为负数")
    private Integer maxnumber;

    /** 房型照片地址数组；以 JSON 数组字符串存储于数据库 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> picture;

    /** 房型具体介绍 */
    private String description;
}