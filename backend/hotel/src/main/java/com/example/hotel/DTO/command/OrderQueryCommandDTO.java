package com.example.hotel.DTO.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员查询订单列表的筛选条件 DTO
 * 各参数均可空，空值表示不作为查询条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderQueryCommandDTO
{
    //房间名称
    private String roomName;

    //时间（代表当晚有人）：按该晚是否有人入住过滤
    private LocalDateTime time;

    //用户电话
    private String telephone;

    //订单状态
    private Integer status;
}