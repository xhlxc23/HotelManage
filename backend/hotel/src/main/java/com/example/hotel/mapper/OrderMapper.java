package com.example.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hotel.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

}