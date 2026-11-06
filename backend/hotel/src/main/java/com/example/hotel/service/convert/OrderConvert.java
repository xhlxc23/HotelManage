package com.example.hotel.service.convert;

import com.example.hotel.DTO.response.UserOrderResponseDTO;
import com.example.hotel.entity.Category;
import com.example.hotel.entity.Order;
import com.example.hotel.entity.Room;

import java.math.BigDecimal;

public class OrderConvert
{
    //订单实体转历史订单响应DTO，room/category 用于补充房间名与房型名
    public static UserOrderResponseDTO toResponse(Order order, Room room, Category category, String userName, String userTelephone)
    {
        return UserOrderResponseDTO.builder()
                .orderId(order.getId())
                .userId(order.getUserAccountId())
                .userName(userName)
                .userTelephone(userTelephone)
                .roomId(order.getRoomId())
                .roomName(room != null ? room.getName() : null)
                .categoryName(category != null ? category.getName() : null)
                .status(order.getStatus())
                .orderTime(order.getOrderTime())
                .checkInTime(order.getStartTime())
                .checkOutTime(order.getEndTime())
                .cancelTime(order.getCancelTime())
                .actualStartTime(order.getActualStartTime())
                .actualEndTime(order.getActualEndTime())
                .totalPrice(order.getPrice())
                .viplevel(order.getVipLevelSnapshot())
                .remark(order.getRemark())
                .build();
    }
}