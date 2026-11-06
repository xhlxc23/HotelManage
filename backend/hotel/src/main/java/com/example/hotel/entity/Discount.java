package com.example.hotel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 折扣实体类（discount 表）
 * 联合主键：(viplevel, categoryId)
 * 表示某一 VIP 等级在某房型上的打折幅度。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("discount")
public class Discount {

    /** VIP等级，1普通、2银卡、3金卡、4大师 */
    @NotNull(message = "VIP等级不能为空")
    @Min(value = 1, message = "VIP等级最小为1")
    @Max(value = 4, message = "VIP等级最大为4")
    private Integer viplevel;

    /** 房型id，关联 category.id */
    @NotBlank(message = "房型id不能为空")
    @TableField("categoryId")
    private String categoryId;

    /** 打折幅度，如 0.900 表示 9 折；普通用户(viplevel=1)应为 1.000 */
    @NotNull(message = "折扣幅度不能为空")
    @TableField("vipDiscount")
    private java.math.BigDecimal vipDiscount;
}