package com.example.hotel.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomCreateCommandDTO
{

    @NotBlank(message = "房间名不能为空")
    @Size(max = 10, message = "房间名长度不能超过10个字符")
    private String name;

    // 房型id，关联 category.id
    @NotBlank(message = "房型id不能为空")
    private String categoryId;

    //运营状态：0停用/维修，1可运营；为空默认1（可运营）
    private Integer status;
}