package com.example.hotel.controller;

import com.example.hotel.DTO.command.*;
import com.example.hotel.DTO.response.*;
import com.example.hotel.common.Result;
import com.example.hotel.entity.Category;
import com.example.hotel.service.AdminService;
import com.example.hotel.service.CategoryService;
import com.example.hotel.service.RoomService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class Admin {
    @Resource
    private AdminService adminService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private RoomService roomService;
    @RequestMapping("/login")
    //管理员登录
    public Result<AdminloginResponseDTO> login(@Valid @RequestBody AdminloginCommandDTO commandandDTO)
    {
        AdminloginResponseDTO result = adminService.login(commandandDTO);
        return Result.success(result);
    }
    //根据管理员id获取管理员信息
    @GetMapping("/info")
    public Result<AdminInfoResponseDTO> getAdminInfo(@RequestParam String id)
    {
        return Result.success(adminService.getAdminInfoById(id));
    }

    //新建房型
    @PostMapping("/addcategory")
    public Result<Void> createCategory(@Valid @RequestBody CategoryCreateCommandDTO commandDTO)
    {
        categoryService.createCategory(commandDTO);
        return Result.success();
    }
    //修改房型：只改传入的非空字段，房型名不能为空，房型基础价格和数量不能为空
    @PostMapping("/updatecategory")
    public Result<Void> updateCategory(@RequestBody CategoryCreateCommandDTO commandDTO)
    {
        categoryService.updateCategory(commandDTO);
        return Result.success();
    }
    //删除房型
    @PostMapping("/deletecategory")
    public Result<Void> deleteCategory(@RequestParam("id") String id)
    {
        categoryService.deleteCategory(id);
        return Result.success();
    }

    //新增房间
    @PostMapping("/addroom")
    public Result<Void> createRoom(@Valid @RequestBody RoomCreateCommandDTO commandDTO)
    {
        roomService.createRoom(commandDTO);
        return Result.success();
    }
    //设置房间状态
    @PostMapping("/setroomstatus")
    public Result<Void> setRoomStatus(@Valid @RequestBody RoomUpdataCommandDTO commandDTO)
    {
        roomService.setRoomStatus(commandDTO);
        return Result.success();
    }
    //修改房间房型
    @PostMapping("/updateroomcategory")
    public Result<Void> updateRoomCategory(@Valid @RequestBody RoomUpdateCategoryCommandDTO commandDTO)
    {
        roomService.updateRoomCategory(commandDTO);
        return Result.success();
    }

    //获取所有房间的简化信息（含运营状态）
    @GetMapping("/rooms")
    public Result<List<RoomBriefResponseDTO>> listAllRoomBriefs()
    {
        return Result.success(roomService.listAllRoomBriefs());
    }
    //查看所有订单（支持按房间名称、时间、电话、状态筛选，参数均可空）
    @GetMapping("/allOrders")
    public Result<List<UserOrderResponseDTO>> listAllOrders(@ModelAttribute OrderQueryCommandDTO queryDTO)
    {
        return Result.success(adminService.listAllOrders(queryDTO));
    }
    //查看所有的房间
    @GetMapping("/getallroom")
    public Result<List<RoomDetailResponseDTO>> listAllRooms()
    {
        return Result.success(roomService.listAllRooms());
    }
    //查看单个房间的占用情况
    @GetMapping("/getroomstatus")
    public Result<List<String>> getRoomStatus(@RequestParam("roomId") String roomId)
    {
        return Result.success(roomService.listOccupiedDates(roomId));
    }
    //查看所有房型
    @GetMapping("/getallcategory")
    public Result<List<CategoryResponseDTO>> listAllCategories()
    {
        return Result.success(categoryService.listAllCategories());
    }
    //查询普通账号列表（支持按用户名、电话筛选，参数均可空）
    @PostMapping("/searchusers")
    public Result<List<UserloginResponseDTO>> searchUsers(@RequestBody UserQueryCommandDTO queryDTO)
    {
        return Result.success(adminService.searchUsers(queryDTO));
    }
    //修改订单状态（确认订单，取消订单，完成订单，入住，退房）
    @PostMapping("/setorderstatus")
    public Result<Void> setOrderStatus(@Valid @RequestBody OrderStatusCommandDTO commandDTO)
    {
        adminService.setOrderStatus(commandDTO);
        return Result.success();
    }



}