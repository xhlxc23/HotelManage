package com.example.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 房间实体类（room 表）
 * 记录酒店物理房间及其运营状态。
 * 注意：status 仅表示"停用/可运营"，不代表当前是否入住；
 *       是否可订需通过 room_occupation 时间区间动态查询。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("room")
public class Room {

    /** 房间id（主键），String 类型，走雪花算法由框架自动生成 */
    @TableId(type = IdType.ASSIGN_ID)
    @Size(max = 20, message = "房间id长度不能超过20个字符")
    private String id;

    /** 房间名（如301、302等），建议唯一 */
    @NotBlank(message = "房间名不能为空")
    @Size(max = 10, message = "房间名长度不能超过10个字符")
    private String name;

    /** 运营状态：0停用/维修，1可运营；不表示"当前是否入住" */
    private Integer status;

    /** 房型id，关联 category.id */
    @NotBlank(message = "房型id不能为空")
    @TableField("categoryId")
    private String categoryId;
}