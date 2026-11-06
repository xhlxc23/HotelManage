package com.example.hotel.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 查询可预订房间的请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableRoomQueryCommandDTO
{
    //用户id，只有用户才能查询/订房
    @NotBlank(message = "用户id不能为空")
    private String userId;

    //计划入住开始时间
    @NotNull(message = "入住时间不能为空")
    private LocalDateTime startTime;

    //计划退房时间
    @NotNull(message = "退房时间不能为空")
    private LocalDateTime endTime;
}