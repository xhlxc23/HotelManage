package com.example.hotel.controller;

import com.example.hotel.DTO.command.*;
import com.example.hotel.DTO.response.AvailableRoomResponseDTO;
import com.example.hotel.DTO.response.RoomDetailResponseDTO;
import com.example.hotel.DTO.response.UserOrderResponseDTO;
import com.example.hotel.DTO.response.UserloginResponseDTO;
import com.example.hotel.common.Result;
import com.example.hotel.service.RoomService;
import com.example.hotel.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class User {
    @Resource
    private UserService userService;
    @Resource
    private RoomService roomService;
    @PostMapping("/login")
    //用户登录
    public Result<UserloginResponseDTO> login(@Valid @RequestBody UserloginCommandandDTO commandandDTO)
    {
        UserloginResponseDTO result = userService.login(commandandDTO);
        return Result.success(result);
    }
    //用户注册
    @PostMapping("/register")
    public Result<UserloginResponseDTO> register(@Valid @RequestBody UserRegisterCommandDTO commandandDTO)
    {
        UserloginResponseDTO result = userService.register(commandandDTO);
        return Result.success(result);
    }

    //根据用户id获取用户信息
    @GetMapping("/userInfo")
    public Result<UserloginResponseDTO.UserDetailResponseDTO> getUserInfo(@RequestParam String id)
    {
        UserloginResponseDTO.UserDetailResponseDTO result = userService.getUserInfo(id);
        return Result.success(result);
    }
    //修改用户数据
    @PostMapping("/updateUserInfo")
    public Result<UserloginResponseDTO> updateUserInfo(@Valid @RequestBody UserUpdataInfoCommandDTO commandDTO)
        {
            UserloginResponseDTO result = userService.updateUserInfo(commandDTO);
            return Result.success(result);
        }
    //查询可预订房间（仅用户）
    @PostMapping("/availableRooms")
    public Result<List<AvailableRoomResponseDTO>> listAvailableRooms(@Valid @RequestBody AvailableRoomQueryCommandDTO commandDTO)
    {
        return Result.success(roomService.listAvailableRooms(commandDTO));
    }
    //用户查询历史订单
    @GetMapping("/historyOrders")
    public Result<List<UserOrderResponseDTO>> listHistoryOrders(@RequestParam String id)
    {
        return Result.success(userService.listHistoryOrders(id));
    }
    //用户查询当前所看房间详细信息
    @GetMapping("/oneRoomDetail")
    public Result<RoomDetailResponseDTO> getOneRoomDetail(@RequestParam String userId, @RequestParam String roomId)
    {
        return Result.success(roomService.getRoomDetail(userId, roomId));
    }
    //用户下单
    @PostMapping("/userSetOrder")
    public Result<UserOrderResponseDTO> userSetOrder(@Valid @RequestBody UserOrderCommandDTO commandDTO)
    {
        UserOrderResponseDTO result = userService.userSetOrder(commandDTO);
        return Result.success(result);
    }
    //取消订单
    @PostMapping("/cancelOrder")
    public Result<Void> cancelOrder(@Valid @RequestBody CancelOrderCommandDTO commandDTO)
    {
        userService.cancelOrder(commandDTO);
        return Result.success();
    }

}