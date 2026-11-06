package com.example.hotel.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserloginCommandandDTO {
    @NotBlank(message = "用户手机号不能为空")
    @Size(min=11,max=11,message="手机号长度必须为11个字符")
    private String telephone;
    @NotBlank(message = "密码不能为空")
    @Size(max = 20,min = 6, message = "密码长度必须在6到20个字符之间")
    private String password;


}
