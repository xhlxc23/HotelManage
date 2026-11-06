package com.example.hotel.DTO.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员查询普通账号列表的筛选条件 DTO
 * 各参数均可空，空值表示不作为查询条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserQueryCommandDTO
{
    private String userName;

    private String telephone;
}