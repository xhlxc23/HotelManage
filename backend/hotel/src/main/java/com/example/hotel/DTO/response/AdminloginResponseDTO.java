package com.example.hotel.DTO.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminloginResponseDTO {
    private int roleType;
    private AdminDetailResponseDTO adminTnfo;
    @Builder
    @Data
    public static class AdminDetailResponseDTO {
        private String id;
        private String telephone;
    }
}
