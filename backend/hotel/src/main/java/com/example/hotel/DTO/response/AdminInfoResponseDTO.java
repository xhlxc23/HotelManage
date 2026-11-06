package com.example.hotel.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminInfoResponseDTO {
    private String id;
    private String adminname;
    private String telephone;
}