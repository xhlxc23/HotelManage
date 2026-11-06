package com.example.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 房间占用实体类（room_occupation 表）
 * 记录某个订单在指定时间段内对某个房间的占用。
 * 区间为 [starttime, endtime)，防重叠通过事务锁 room 行 + 查重叠区间实现。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("room_occupation")
public class RoomOccupation {

    /** 占用情况id（主键），String 类型，走雪花算法由框架自动生成 */
    @TableId(type = IdType.ASSIGN_ID)
    @NotBlank(message = "占用id不能为空")
    private String id;

    /** 订单id，关联 orders.id */
    @NotBlank(message = "订单id不能为空")
    @TableField("orderId")
    private String orderId;

    /** 占用开始时间；区间为[starttime, endtime) */
    @NotNull(message = "占用开始时间不能为空")
    private LocalDateTime starttime;

    /** 占用结束时间 */
    @NotNull(message = "占用结束时间不能为空")
    private LocalDateTime endtime;

    /** 房间id，关联 room.id */
    @NotBlank(message = "房间id不能为空")
    @TableField("roomId")
    private String roomId;
}