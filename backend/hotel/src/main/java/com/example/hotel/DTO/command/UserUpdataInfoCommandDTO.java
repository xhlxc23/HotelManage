package com.example.hotel.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户修改资料请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdataInfoCommandDTO {

    @NotBlank(message = "用户id不能为空")
    @Size(max = 20, message = "用户id长度不能超过20个字符")
    private String id;

    @Size(min = 1, max = 20, message = "用户名长度必须在1-20个字符之间")
    private String username;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Size(min = 11, max = 11, message = "手机号长度必须为11个字符")
    private String telephone;

    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;
}