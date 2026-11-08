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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("panorama_node")
public class PanoramaNode {

    @TableId(type = IdType.ASSIGN_ID)
    @Size(max = 32, message = "节点id长度不能超过32个字符")
    private String id;

    @NotBlank(message = "房型id不能为空")
    @TableField("category_id")
    private String categoryId;

    @TableField("image_url")
    private String imageUrl;

    private String name;

    @TableField("is_start")
    private Integer isStart;
}