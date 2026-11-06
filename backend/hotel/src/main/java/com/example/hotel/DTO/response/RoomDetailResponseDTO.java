package com.example.hotel.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 单个房间的详细信息 DTO（用户浏览房间详情用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDetailResponseDTO {

    private String id;
    //房间名称
    private String roomName;
    //房型id
    private String categoryId;
    //房型名称
    private String categoryName;
    //房型所有图片地址
    private List<String> pictures;
    //每晚基础价格
    private BigDecimal basePrice;
    //优惠后每晚价格
    private BigDecimal priceAfterDiscount;
    //房型描述
    private String description;
}