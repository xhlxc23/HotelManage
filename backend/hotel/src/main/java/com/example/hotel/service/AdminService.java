package com.example.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hotel.DTO.command.AdminloginCommandDTO;
import com.example.hotel.DTO.command.OrderQueryCommandDTO;
import com.example.hotel.DTO.command.OrderStatusCommandDTO;
import com.example.hotel.DTO.command.UserQueryCommandDTO;
import com.example.hotel.DTO.response.AdminInfoResponseDTO;
import com.example.hotel.DTO.response.AdminloginResponseDTO;
import com.example.hotel.DTO.response.UserOrderResponseDTO;
import com.example.hotel.DTO.response.UserloginResponseDTO;
import com.example.hotel.common.Result;
import com.example.hotel.entity.Admin;
import com.example.hotel.entity.Category;
import com.example.hotel.entity.Order;
import com.example.hotel.entity.Room;
import com.example.hotel.entity.User;
import com.example.hotel.exception.BusionessException;
import com.example.hotel.exception.adminloginException;
import com.example.hotel.mapper.AdminMapper;
import com.example.hotel.mapper.CategoryMapper;
import com.example.hotel.mapper.OrderMapper;
import com.example.hotel.mapper.RoomMapper;
import com.example.hotel.mapper.UserMapper;
import com.example.hotel.service.convert.AdminConvert;
import com.example.hotel.service.convert.OrderConvert;
import com.example.hotel.service.convert.UserConvert;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {
    //管理员登录
    @Resource
    private AdminMapper adminMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private RoomMapper roomMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private CategoryMapper categoryMapper;
    public AdminloginResponseDTO login(AdminloginCommandDTO commandDTO)
    {
        LambdaQueryWrapper<Admin> QueryWrapper =new LambdaQueryWrapper<>();
        QueryWrapper.eq(Admin::getTelephone,commandDTO.getTelephone());
        Admin admin = adminMapper.selectOne(QueryWrapper);
        if(admin==null)
        {
            throw new adminloginException("管理员不存在");
        }
        if(!admin.getPassword().equals(commandDTO.getPassword()))
        {
            throw new adminloginException("密码错误");
        }
        //登录成功
        return AdminConvert.toResponse(admin);
    }
    // 管理员查询所有订单，支持按房间名称、时间（当晚有人）、电话、状态筛选；参数可空；结果按 status、orderTime 排序
    public List<UserOrderResponseDTO> listAllOrders(OrderQueryCommandDTO queryDTO)
    {
        //联表条件1：按房间名称 -> 查出匹配的 roomId 集合
        List<String> roomIds = null;
        if (queryDTO.getRoomName() != null && !queryDTO.getRoomName().isBlank())
        {

            roomIds = roomMapper.selectList(
                            new LambdaQueryWrapper<Room>().eq(Room::getName, queryDTO.getRoomName()))
                    .stream().map(Room::getId).collect(Collectors.toList());
            //房间名无匹配，直接返回空
            if (roomIds.isEmpty())
            {
                return Collections.emptyList();
            }
        }
        //联表条件2：按电话 -> 查出匹配的 userId 集合
        List<String> userIds = null;
        if (queryDTO.getTelephone() != null && !queryDTO.getTelephone().isBlank())
        {
            userIds = userMapper.selectList(
                            new LambdaQueryWrapper<User>().eq(User::getTelephone, queryDTO.getTelephone()))
                    .stream().map(User::getId).collect(Collectors.toList());
            //电话无匹配，直接返回空
            if (userIds.isEmpty())
            {
                return Collections.emptyList();
            }
        }

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        //房间名条件
        if (roomIds != null)
        {
            wrapper.in(Order::getRoomId, roomIds);
        }
        //电话条件
        if (userIds != null)
        {
            wrapper.in(Order::getUserAccountId, userIds);
        }
        //状态条件
        if (queryDTO.getStatus() != null)
        {
            wrapper.eq(Order::getStatus, queryDTO.getStatus());
        }
        //时间条件：代表当晚有人，即订单占用时间段与目标晚区间有重叠
        if (queryDTO.getTime() != null)
        {
            //目标晚区间近似取 [time, time+1天)
            wrapper.lt(Order::getStartTime, queryDTO.getTime().plusDays(1))
                    .gt(Order::getEndTime, queryDTO.getTime());
        }
        //排序：先 status 后 orderTime，均升序
        wrapper.orderByAsc(Order::getStatus)
                .orderByAsc(Order::getOrderTime);

        List<Order> orders = orderMapper.selectList(wrapper);
        if (orders == null || orders.isEmpty())
        {
            return new ArrayList<>();
        }
        return orders.stream().map(order -> {
            Room room = roomMapper.selectById(order.getRoomId());
            Category category = room != null ? categoryMapper.selectById(room.getCategoryId()) : null;
            User user = userMapper.selectById(order.getUserAccountId());
            String userName = user != null ? user.getUsername() : null;
            String userTelephone = user != null ? user.getTelephone() : null;
            return OrderConvert.toResponse(order, room, category, userName, userTelephone);
        }).collect(Collectors.toList());
    }

    //管理员查询所有普通账号，支持按用户名（模糊）、电话（精确）筛选；参数可空
    public List<UserloginResponseDTO> searchUsers(UserQueryCommandDTO queryDTO)
    {
        boolean hasUser = queryDTO.getUserName() != null && !queryDTO.getUserName().isBlank();
        boolean hasTel = queryDTO.getTelephone() != null && !queryDTO.getTelephone().isBlank();
        //无任何筛选条件时，直接返回空，避免查出所有用户
        if (!hasUser && !hasTel)
        {
            throw new BusionessException("用户名或电话不能为空");
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        //用户名条件：模糊匹配
        if (hasUser)
        {
            wrapper.eq(User::getUsername, queryDTO.getUserName());
        }
        //电话条件：精确匹配
        else if (hasTel)
        {
            wrapper.eq(User::getTelephone, queryDTO.getTelephone());
        }
        List<User> users = userMapper.selectList(wrapper);
        if (users == null || users.isEmpty())
        {
            throw new BusionessException("未查询到匹配用户");
        }
        return users.stream()
                .map(user -> UserloginResponseDTO.builder()
                        .roleType(1)
                        .userTnfo(UserConvert.toDetail(user))
                        .build())
                .collect(Collectors.toList());
    }
    //修改订单状态（确认订单，取消订单，完成订单，入住，退房）
    public void setOrderStatus(OrderStatusCommandDTO commandDTO)
    {
        //校验订单存在
        Order order = orderMapper.selectById(commandDTO.getOrderId());
        if (order == null)
        {
            throw new BusionessException("订单不存在");
        }
        //更新订单状态
        order.setStatus(commandDTO.getStatus());
        if (commandDTO.getStatus() == 2)
        {
            order.setActualStartTime(LocalDateTime.now());
        }
        else if (commandDTO.getStatus() == 3)
        {
            order.setActualEndTime(LocalDateTime.now());
            //修改用户经验值
            User user = userMapper.selectById(order.getUserAccountId());
            user.addExp(order.getPrice().intValue());
            user.updateVipLevelByExp();
            userMapper.updateById(user);
        }
        else if (commandDTO.getStatus() == 5)
        {
            order.setCancelTime(LocalDateTime.now());
        }
        orderMapper.updateById(order);
    }

    public AdminInfoResponseDTO getAdminInfoById(String id)
    {
        Admin admin = adminMapper.selectById(id);
        if (admin == null)
        {
            throw new BusionessException("管理员不存在");
        }
        return AdminInfoResponseDTO.builder()
                .id(admin.getId())
                .adminname(admin.getAdminname())
                .telephone(admin.getTelephone())
                .build();
    }
}