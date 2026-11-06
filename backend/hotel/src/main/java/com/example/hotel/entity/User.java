package com.example.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户账户实体类（user_account 表）
 * 对应数据库表：user_account
 *
 * 使用说明：
 *  1. @Data / @Builder 等 Lombok 注解：自动生成 getter/setter、builder 构造器、
 *     无参构造器和全参构造器，简化实体类的开发，避免手写大量样板代码。
 *  2. MyBatis-Plus 注解：@TableName 映射表名，@TableId 标记主键。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_account")
public class User {

    /** 用户id（主键），String 类型，走雪花算法由框架自动生成 */
    @TableId(type = IdType.ASSIGN_ID)
    @Size(max = 20, message = "用户id长度不能超过20个字符")
    private String id;

    /** 用户姓名，必填，长度 1~20 个字符 */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 1, max = 20, message = "用户名长度必须在1-20个字符之间")
    private String username;

    /**
     * vip 等级（1普通、2银卡、3金卡、4大师）
     * 本字段由积分 exp 推导而来，而不是独立录入，
     * 退房完成时应调用 updateVipLevelByExp() 在事务内同步更新。
     */
    @Min(value = 1, message = "vip等级最小为1")
    @Max(value = 4, message = "vip等级最大为4")
    private Integer viplevel;

    /** 用户电话，必填，格式须为 11 位中国大陆手机号 */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Size(max = 13, message = "手机号长度不能超过13个字符")
    private String telephone;

    /** 用户账户密码（不加密），必填，长度 6~20 个字符 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    /** 用户积分（消费一元获得一积分），不可为负数 */
    @PositiveOrZero(message = "积分不能为负数")
    private Integer exp;

    //添加经验值
    public void addExp(Integer exp)
    {
        this.exp += exp;
    }

    //根据用户经验值更新vip等级
    public void updateVipLevelByExp() {
        if (exp == null || exp < 100) {
            this.viplevel = 1;
        } else if (exp < 1000) {
            this.viplevel = 2;
        } else if (exp < 5000) {
            this.viplevel = 3;
        } else {
            this.viplevel = 4;
        }
    }
}