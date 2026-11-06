package com.example.hotel.DTO.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserloginResponseDTO {
    private int roleType;
    private UserDetailResponseDTO userTnfo;

    @Builder
    @Data
    public static class UserDetailResponseDTO {
        private String id;
        private String username;
        private Integer viplevel;
        private String telephone;
        private Integer exp;
    }
}