package com.example.hotel.DTO.command;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomUpdateCategoryCommandDTO {
    @NotBlank(message = "房间id不能为空")
    private String id;

    @NotBlank(message = "房型id不能为空")
    private String categoryId;
}