package com.example.hotel.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterCommandDTO {

    @NotBlank(message = "手机号不能为空")
    @Size(min = 11, max = 11, message = "手机号长度必须为11位")
    @Pattern(regexp = "^1[3456789]\\d{9}$", message = "手机号格式错误")
    private  String telephone;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6到20位之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "密码只能包含字母、数字和下划线")
    private  String password;
    @NotBlank(message = "用户名不能为空")
    @Size(min = 1, max = 20, message = "用户名长度必须在1到20位之间")
    private  String username;

    private Integer viplevel=1;
    private Integer exp=0;
}
