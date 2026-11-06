package com.example.hotel.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomUpdataCommandDTO
{
    @NotBlank(message = "房间id不能为空")
    private String id;
    @NotNull(message = "房间状态不能为空")
    private Integer status;
}