package com.example.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类（orders 表）
 * 记录一次订房交易的完整信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("orders")
public class Order {

    /** 订单id（主键），String 类型，走雪花算法由框架自动生成 */
    @TableId(type = IdType.ASSIGN_ID)
    @NotBlank(message = "订单id不能为空")
    private String id;

    /** 用户账户id，关联 user_account.id */
    @NotBlank(message = "用户账户id不能为空")
    @TableField("user_accountid")
    private String userAccountId;

    /** 房间id，关联 room.id */
    @NotBlank(message = "房间id不能为空")
    @TableField("roomId")
    private String roomId;

    /** 下单时间 */
    @NotNull(message = "下单时间不能为空")
    @TableField("orderTime")
    private LocalDateTime orderTime;

    /** 订单状态：0待确认，1已确认未入住，2已入住未退房，3已退房完成，4已取消，5已拒绝，6已过期 */
    private Integer status;

    /** 预定的入住开始时间 */
    @TableField("startTime")
    private LocalDateTime startTime;

    /** 预定的退房时间 */
    @TableField("endTime")
    private LocalDateTime endTime;

    /** 订单取消时间，可空 */
    @TableField("cancelTime")
    private LocalDateTime cancelTime;

    /** 实际的入住时间，可空 */
    @TableField("actualStartTime")
    private LocalDateTime actualStartTime;

    /** 实际的退房时间，可空 */
    @TableField("actualEndTime")
    private LocalDateTime actualEndTime;

    /** 下单时的会员等级快照 */
    @TableField("vipLevelSnapshot")
    private Integer vipLevelSnapshot;

    /** 最终结算价 */
    @DecimalMin(value = "0", inclusive = true, message = "结算价不能为负数")
    private BigDecimal price;

    /** 客人备注 */
    private String remark;
}