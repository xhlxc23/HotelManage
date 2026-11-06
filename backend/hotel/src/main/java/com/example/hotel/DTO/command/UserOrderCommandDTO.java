package com.example.hotel.DTO.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户下单的请求体 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOrderCommandDTO
{
    //用户账户id
    private String user_accountId;

    //房间id
    private String roomId;

    //下单时间
    private LocalDateTime orderTime;

    //入住时间
    private LocalDateTime startTime;

    //退房时间
    private LocalDateTime endTime;

    //下单时会员等级快照
    private Integer vipLevelSnapshot;

    //结算价
    private BigDecimal price;

    //备注
    private String remark;
}