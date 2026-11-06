package com.example.hotel.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOrderResponseDTO
{
    private String orderId;
    private String userId;
    private String userName;
    private String userTelephone;
    private String roomId;
    private String roomName;
    private String categoryName;
    private Integer status;
    private LocalDateTime orderTime;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private LocalDateTime cancelTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private BigDecimal totalPrice;
    private Integer viplevel;
    private String remark;
}