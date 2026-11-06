package com.example.hotel.DTO.command;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 新建/修改房型请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateCommandDTO {

    /** 房型id（修改房型信息时用于定位目标，新建时无需传） */
    private String id;

    /** 房型名（如豪华大床房） */
    @NotBlank(message = "房型名不能为空")
    private String name;

    /** 房型基础价格（元/天） */
    @NotNull(message = "基础价格不能为空")
    @DecimalMin(value = "0", inclusive = true, message = "基础价格不能为负数")
    private BigDecimal basicPrice;

    /** 各 VIP 等级的折扣配置（1-4 级均需提供） */
    @NotNull(message = "VIP折扣不能为空")
    @Size(min = 1, max = 4, message = "VIP折扣需提供1至4个")
    private List<DiscountCommandDTO> discounts;

    /** 房型照片地址数组 */
    private List<String> picture;

    /** 房型介绍 */
    private String description;
}