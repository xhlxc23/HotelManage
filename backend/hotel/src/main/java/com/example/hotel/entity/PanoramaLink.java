package com.example.hotel.entity;

import java.math.BigDecimal;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("panorama_link")
public class PanoramaLink {

    @TableId(type = IdType.ASSIGN_ID)
    @Size(max = 32, message = "链接id长度不能超过32个字符")
    private String id;

    @NotBlank(message = "起始节点id不能为空")
    @TableField("source_node_id")
    private String sourceNodeId;

    @NotBlank(message = "目标节点id不能为空")
    @TableField("target_node_id")
    private String targetNodeId;

    private BigDecimal yaw;

    private BigDecimal pitch;

    private String label;
}