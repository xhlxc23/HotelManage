package com.example.hotel.DTO.command;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 房型对应某 VIP 等级的折扣
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountCommandDTO {

    /** VIP等级：1普通、2银卡、3金卡、4大师 */
    @NotNull(message = "VIP等级不能为空")
    @Min(value = 1, message = "VIP等级最小为1")
    @Max(value = 4, message = "VIP等级最大为4")
    private Integer viplevel;

    /** 打折幅度，如 0.900 表示 9 折；普通用户(viplevel=1)应为 1.000 */
    @NotNull(message = "折扣幅度不能为空")
    private BigDecimal vipDiscount;
}