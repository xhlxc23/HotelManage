package com.example.hotel.DTO.command;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户修改订单状态的请求体 DTO（支持取消/确认订单等用户侧状态变更）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderCommandDTO
{
    @NotNull(message = "订单id不能为空")
    private String id;

    @NotNull(message = "订单状态不能为空")
    private Integer status;
}