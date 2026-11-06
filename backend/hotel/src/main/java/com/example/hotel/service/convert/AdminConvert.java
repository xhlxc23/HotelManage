package com.example.hotel.service.convert;

import com.example.hotel.DTO.response.AdminloginResponseDTO;
import com.example.hotel.entity.Admin;

public class AdminConvert {
    public static AdminloginResponseDTO.AdminDetailResponseDTO toDetailResponse(Admin admin)
    {
        return AdminloginResponseDTO.AdminDetailResponseDTO.builder()
                .id(admin.getId())
                .telephone(admin.getTelephone())
                .build();
    }
    public static AdminloginResponseDTO toResponse(Admin admin)
    {
        return AdminloginResponseDTO.builder()
                .roleType(2)
                .adminTnfo(toDetailResponse(admin))
                .build();
    }
}
