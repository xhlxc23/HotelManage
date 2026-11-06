package com.example.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员账户实体类（admin_account 表）
 * 对应数据库表：admin_account
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("admin_account")
public class Admin {

    /** 管理员id（主键），String 类型，走雪花算法由框架自动生成 */
    @TableId(type = IdType.ASSIGN_ID)
    @Size(max = 20, message = "管理员id长度不能超过20个字符")
    private String id;

    /** 管理员电话，最大长度 13 个字符 */
    @NotBlank(message = "管理员电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "管理员电话格式不正确")
    @Size(max = 13, message = "管理员电话长度不能超过13个字符")
    private String telephone;

    /** 管理员账户密码（不加密），必填，长度 6~20 个字符 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    /** 管理员姓名，必填，长度 1~20 个字符 */
    @NotBlank(message = "管理员姓名不能为空")
    @Size(min = 1, max = 20, message = "管理员姓名长度必须在1-20个字符之间")
    private String adminname;
}