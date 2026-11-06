package com.example.hotel.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 可运营房间的简化信息 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomBriefResponseDTO {

    private String id;
    private String roomName;
    private Integer status;
    private String categoryId;
    private String categoryName;
    private String picture;
    private String description;
}