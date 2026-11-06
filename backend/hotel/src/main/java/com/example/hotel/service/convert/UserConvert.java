package com.example.hotel.service.convert;

import com.example.hotel.DTO.command.UserRegisterCommandDTO;
import com.example.hotel.DTO.response.UserloginResponseDTO;
import com.example.hotel.entity.User;

public class UserConvert
{
    public static UserloginResponseDTO.UserDetailResponseDTO toDetail(User user) {
        return UserloginResponseDTO.UserDetailResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .viplevel(user.getViplevel())
                .telephone(user.getTelephone())
                .exp(user.getExp())
                .build();
    }
    //精简版：登录/注册返回用，只返回基础字段
    public static UserloginResponseDTO.UserDetailResponseDTO toBrief(User user) {
        return UserloginResponseDTO.UserDetailResponseDTO.builder()
                .id(user.getId())
                .telephone(user.getTelephone())
                .build();
    }
    public static UserloginResponseDTO toResponse(User user) {
        return UserloginResponseDTO.builder()
                .roleType(1)
                .userTnfo(toBrief(user))
                .build();

    }
    public static User registerToEntity(UserRegisterCommandDTO commandDTO) {
        return User.builder()
                .telephone(commandDTO.getTelephone())
                .password(commandDTO.getPassword())
                .username(commandDTO.getUsername())
                .viplevel(commandDTO.getViplevel())
                .exp(commandDTO.getExp())
                .build();
    }

}