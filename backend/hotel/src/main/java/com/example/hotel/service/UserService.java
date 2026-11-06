package com.example.hotel.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.example.hotel.DTO.command.CancelOrderCommandDTO;
import com.example.hotel.DTO.command.UserOrderCommandDTO;
import com.example.hotel.DTO.command.UserRegisterCommandDTO;
import com.example.hotel.DTO.command.UserUpdataInfoCommandDTO;
import com.example.hotel.DTO.command.UserloginCommandandDTO;
import com.example.hotel.DTO.response.UserOrderResponseDTO;
import com.example.hotel.DTO.response.UserloginResponseDTO;
import com.example.hotel.common.Result;
import com.example.hotel.common.ResultCode;
import com.example.hotel.entity.Category;
import com.example.hotel.entity.Order;
import com.example.hotel.entity.Room;
import com.example.hotel.entity.RoomOccupation;
import com.example.hotel.entity.User;
import com.example.hotel.exception.BusionessException;
import com.example.hotel.exception.registerException;
import com.example.hotel.mapper.CategoryMapper;
import com.example.hotel.mapper.OrderMapper;
import com.example.hotel.mapper.RoomMapper;
import com.example.hotel.mapper.UserMapper;
import com.example.hotel.mapper.RoomOccupationMapper;
import com.example.hotel.service.convert.OrderConvert;
import com.example.hotel.service.convert.UserConvert;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private RoomMapper roomMapper;
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private RoomOccupationMapper roomOccupationMapper;
    public UserloginResponseDTO login(UserloginCommandandDTO commandDTO)
    {
        //构建查询条件
        LambdaQueryWrapper<User> QueryWrapper =new LambdaQueryWrapper<>();
        QueryWrapper.eq(User::getTelephone,commandDTO.getTelephone());
        User user=userMapper.selectOne(QueryWrapper);
        System.out.println(user);
        //判断用户是否存在
        if(user==null) {
            throw new BusionessException("用户不存在");
        }
        //判断密码是否正确
        String inputPassword=commandDTO.getPassword().trim();
        if(!inputPassword.equals(user.getPassword().trim())) {
            throw new BusionessException("密码错误");
        }
        //返回登录成功
        UserloginResponseDTO responseDTO=UserConvert.toResponse(user);
        return responseDTO;
    }

    public UserloginResponseDTO register(UserRegisterCommandDTO commandDTO)
    {
        //判断用户是否存在
        LambdaQueryWrapper<User> QueryWrapper =new LambdaQueryWrapper<>();
        QueryWrapper.eq(User::getTelephone,commandDTO.getTelephone());
        User user=userMapper.selectOne(QueryWrapper);
        if(user!=null)
        {
            throw new registerException("用户已存在");
        }
        //创建用户
        User newUser=UserConvert.registerToEntity(commandDTO);
        userMapper.insert(newUser);

        return UserConvert.toResponse(newUser);
    }

    //根据用户id获取用户信息
    public UserloginResponseDTO.UserDetailResponseDTO getUserInfo(String id)
    {
        User user = userMapper.selectById(id);
        //判断用户是否存在
        if(user==null) {
            throw new BusionessException("用户不存在");
        }
        return UserConvert.toDetail(user);
    }
    //根据用户id更新用户信息
    public UserloginResponseDTO updateUserInfo(UserUpdataInfoCommandDTO commandDTO)
    {
        User user = userMapper.selectById(commandDTO.getId());
        //判断用户是否存在
        if(user==null) {
            throw new BusionessException("用户不存在");
        }
        //更新用户信息：仅修改传入的非空字段，空字段保留原值
        if (commandDTO.getUsername() != null && !commandDTO.getUsername().isBlank()) {
            user.setUsername(commandDTO.getUsername());
        }
        if (commandDTO.getTelephone() != null && !commandDTO.getTelephone().isBlank()) {
            user.setTelephone(commandDTO.getTelephone());
        }
        if (commandDTO.getPassword() != null && !commandDTO.getPassword().isBlank()) {
            user.setPassword(commandDTO.getPassword());
        }
        userMapper.updateById(user);
        return UserConvert.toResponse(user);
    }
    //根据用户id查询历史订单
    public List<UserOrderResponseDTO> listHistoryOrders(String id)
    {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getUserAccountId, id);
        List<Order> orders = orderMapper.selectList(queryWrapper);
        return orders.stream()
                .map(order -> {
                    User user = userMapper.selectById(order.getUserAccountId());
                    Room room = roomMapper.selectById(order.getRoomId());
                    Category category =categoryMapper.selectById(room.getCategoryId());
                    return OrderConvert.toResponse(order, room, category, user.getUsername(), user.getTelephone());
                })
                .toList();
    }

    //用户下单：校验后创建订单与房间占用记录
    @Transactional
    public UserOrderResponseDTO userSetOrder(UserOrderCommandDTO commandDTO)
    {
        //校验用户存在
        User user = userMapper.selectById(commandDTO.getUser_accountId());
        if (user == null)
        {
            throw new BusionessException("用户不存在");
        }
        //校验房间存在且可运营
        Room room = roomMapper.selectById(commandDTO.getRoomId());
        if (room == null)
        {
            throw new BusionessException("房间不存在");
        }
        if (room.getStatus() == null || room.getStatus() != 1)
        {
            throw new BusionessException("房间不可运营");
        }
        //校验时间
        if (!commandDTO.getStartTime().isBefore(commandDTO.getEndTime()))
        {
            throw new BusionessException("入住时间必须早于退房时间");
        }
        //校验目标区间是否与已占用区间重叠
        Long occupiedCount = roomOccupationMapper.selectCount(
                new LambdaQueryWrapper<RoomOccupation>()
                        .eq(RoomOccupation::getRoomId, room.getId())
                        .lt(RoomOccupation::getStarttime, commandDTO.getEndTime())
                        .gt(RoomOccupation::getEndtime, commandDTO.getStartTime()));
        if (occupiedCount != null && occupiedCount > 0)
        {
            throw new BusionessException("该时间段房间已被占用");
        }
        //创建订单（状态默认0待确认）
        Order order = Order.builder()
                .userAccountId(commandDTO.getUser_accountId())
                .roomId(commandDTO.getRoomId())
                .orderTime(commandDTO.getOrderTime())
                .status(0)
                .startTime(commandDTO.getStartTime())
                .endTime(commandDTO.getEndTime())
                .vipLevelSnapshot(user.getViplevel())
                .price(commandDTO.getPrice())
                .remark(commandDTO.getRemark())
                .build();
        orderMapper.insert(order);
        //创建房间占用记录
        RoomOccupation occupation = RoomOccupation.builder()
                .orderId(order.getId())
                .roomId(commandDTO.getRoomId())
                .starttime(commandDTO.getStartTime())
                .endtime(commandDTO.getEndTime())
                .build();
        roomOccupationMapper.insert(occupation);
        //返回订单信息
        Category category = categoryMapper.selectById(room.getCategoryId());
        return OrderConvert.toResponse(order, room, category, user.getUsername(), user.getTelephone());
    }

    //用户修改订单状态（取消或确认）
    @Transactional
    public void cancelOrder(CancelOrderCommandDTO commandDTO)
    {
        //校验订单存在
        Order order = orderMapper.selectById(commandDTO.getId());
        if (order == null)
        {
            throw new BusionessException("订单不存在");
        }
        //业务校验：只有待确认（0）的订单才能由用户取消或确认
        if (order.getStatus() != 0)
        {
            throw new BusionessException("当前订单状态不允许操作");
        }
        //状态校验：用户侧只允许转为已确认（1）或已取消（4）
        int targetStatus = commandDTO.getStatus();
        if (targetStatus != 1 && targetStatus != 4)
        {
            throw new BusionessException("无效的订单状态");
        }
        //更新订单状态
        order.setStatus(targetStatus);
        if (targetStatus == 4)
        {
            order.setCancelTime(LocalDateTime.now());
        }
        orderMapper.updateById(order);
        //取消订单时释放对应房间占用记录
        if (targetStatus == 4)
        {
            roomOccupationMapper.delete(
                    new LambdaQueryWrapper<RoomOccupation>()
                            .eq(RoomOccupation::getOrderId, order.getId()));
        }
    }


}