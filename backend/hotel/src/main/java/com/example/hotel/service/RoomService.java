package com.example.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hotel.DTO.command.AvailableRoomQueryCommandDTO;
import com.example.hotel.DTO.command.RoomCreateCommandDTO;
import com.example.hotel.DTO.command.RoomUpdataCommandDTO;
import com.example.hotel.DTO.command.RoomUpdateCategoryCommandDTO;
import com.example.hotel.DTO.response.AvailableRoomResponseDTO;
import com.example.hotel.DTO.response.RoomBriefResponseDTO;
import com.example.hotel.DTO.response.RoomDetailResponseDTO;
import com.example.hotel.common.Result;
import com.example.hotel.entity.Category;
import com.example.hotel.entity.Discount;
import com.example.hotel.entity.Room;
import com.example.hotel.entity.RoomOccupation;
import com.example.hotel.entity.User;
import com.example.hotel.exception.BusionessException;
import com.example.hotel.mapper.CategoryMapper;
import com.example.hotel.mapper.DiscountMapper;
import com.example.hotel.mapper.RoomOccupationMapper;
import com.example.hotel.mapper.RoomMapper;
import com.example.hotel.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class RoomService {

    @Resource
    private RoomMapper roomMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private DiscountMapper discountMapper;

    @Resource
    private RoomOccupationMapper roomOccupationMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private CategoryService categoryService;


    public void createRoom(RoomCreateCommandDTO commandDTO)
    {
        categoryService.requireCategoryExists(commandDTO.getCategoryId());
        Room room = Room.builder()
                .name(commandDTO.getName())
                .categoryId(commandDTO.getCategoryId())
                .status(commandDTO.getStatus() == null ? 1 : commandDTO.getStatus())
                .build();
        roomMapper.insert(room);
    }
    public void setRoomStatus(RoomUpdataCommandDTO commandDTO)
    {
        Room room = roomMapper.selectById(commandDTO.getId());
        if (room == null)
        {
            throw new BusionessException("房间不存在");
        }
        room.setStatus(commandDTO.getStatus());
        Category category = categoryMapper.selectById(room.getCategoryId());
        if(commandDTO.getStatus() == 1)
        {
            category.setMaxnumber(category.getMaxnumber() + 1);
        }
        else
        {
            category.setMaxnumber(category.getMaxnumber() - 1);
        }
        categoryMapper.updateById(category);
        roomMapper.updateById(room);
    }

    public void updateRoomCategory(RoomUpdateCategoryCommandDTO commandDTO)
    {
        Room room = roomMapper.selectById(commandDTO.getId());
        if (room == null)
        {
            throw new BusionessException("房间不存在");
        }
        categoryService.requireCategoryExists(commandDTO.getCategoryId());
        if (commandDTO.getCategoryId().equals(room.getCategoryId()))
        {
            return;
        }
        if (room.getStatus() != null && room.getStatus() == 1)
        {
            Category oldCategory = categoryMapper.selectById(room.getCategoryId());
            Category newCategory = categoryMapper.selectById(commandDTO.getCategoryId());
            if (oldCategory != null)
            {
                oldCategory.setMaxnumber(oldCategory.getMaxnumber() - 1);
                categoryMapper.updateById(oldCategory);
            }
            if (newCategory != null)
            {
                newCategory.setMaxnumber(newCategory.getMaxnumber() + 1);
                categoryMapper.updateById(newCategory);
            }
        }
        room.setCategoryId(commandDTO.getCategoryId());
        roomMapper.updateById(room);
    }

    /**
     * 获取所有房间的简化信息（含运营状态）：
     * 房间id、房间名、状态 + 所属房型的id、名称、首张图片、描述
     */
    public List<RoomBriefResponseDTO> listAllRoomBriefs()
    {
        List<Room> rooms = roomMapper.selectList(null);
        if (rooms == null || rooms.isEmpty())
        {
            return Collections.emptyList();
        }
        return rooms.stream().map(room ->
        {
            Category category = categoryMapper.selectById(room.getCategoryId());
            String picture = null;
            if (category != null && category.getPicture() != null && !category.getPicture().isEmpty()) {
                picture = category.getPicture().get(0);
            }
            return RoomBriefResponseDTO.builder()
                    .id(room.getId())
                    .roomName(room.getName())
                    .status(room.getStatus())
                    .categoryId(room.getCategoryId())
                    .categoryName(category.getName())
                    .picture(picture)
                    .description(category.getDescription())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 根据用户id和入住、退房时间查询可预订房间：
     * 1. 校验用户存在（只有用户才能查询/订房）
     * 2. 查所有可运营房间（status=1）
     * 3. 过滤掉目标区间内已被占用的房间
     * 4. 计算优惠后每晚价格 = 房型基础价 × 该用户viplevel的折扣
     */
    public List<AvailableRoomResponseDTO> listAvailableRooms(AvailableRoomQueryCommandDTO commandDTO)
    {
        //校验用户存在
        User user = userMapper.selectById(commandDTO.getUserId());
        if (user == null)
        {
            throw new BusionessException("用户不存在");
        }
        //校验时间
        if (!commandDTO.getStartTime().isBefore(commandDTO.getEndTime()))
        {
            throw new BusionessException("入住时间必须早于退房时间");
        }
        //查所有可运营房间
        List<Room> rooms = roomMapper.selectList(
                new LambdaQueryWrapper<Room>().eq(Room::getStatus, 1));
        if (rooms == null || rooms.isEmpty())
        {
            return Collections.emptyList();
        }
        List<AvailableRoomResponseDTO> result = new ArrayList<>();
        for (Room room : rooms)
        {
            //过滤：目标区间内已被占用的房间不可订（判断占用区间与查询区间是否重叠）
            Long occupiedCount = roomOccupationMapper.selectCount(
                    new LambdaQueryWrapper<RoomOccupation>()
                            .eq(RoomOccupation::getRoomId, room.getId())
                            .lt(RoomOccupation::getStarttime, commandDTO.getEndTime())
                            .gt(RoomOccupation::getEndtime, commandDTO.getStartTime()));
            if (occupiedCount != null && occupiedCount > 0)
            {
                continue;
            }
            result.add(buildAvailableRoom(room, user));
        }
        return result;
    }

    //组装单个可预订房间信息
    private AvailableRoomResponseDTO buildAvailableRoom(Room room, User user)
    {
        Category category = categoryMapper.selectById(room.getCategoryId());
        String picture = null;
        if (category != null && category.getPicture() != null && !category.getPicture().isEmpty())
        {
            picture = category.getPicture().get(0);
        }
        //查该用户viplevel在该房型上的折扣；查不到按原价(1.0)
        BigDecimal discountRate = getVipDiscountRate(category, user.getViplevel());
        //优惠后价格 = 基础价 × 折扣；原价取房型基本价
        BigDecimal basicPrice = null;
        BigDecimal priceAfterDiscount = BigDecimal.ZERO;
        if (category != null && category.getBasicPrice() != null)
        {
            basicPrice = category.getBasicPrice();
            priceAfterDiscount = basicPrice.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        }
        return AvailableRoomResponseDTO.builder()
                .id(room.getId())
                .roomName(room.getName())
                .categoryId(room.getCategoryId())
                .categoryName( category.getName())
                .picture(picture)
                .description(category.getDescription())
                .oneNightPrice(basicPrice)
                .priceAfterDiscount(priceAfterDiscount)
                .build();
    }

    //根据房间id获取房间详情（用户浏览）：返回房间id、名称、房型所有图片、房型名称、优惠后每晚价格、每晚基础价格、房型描述
    public RoomDetailResponseDTO getRoomDetail(String userId, String roomId)
    {
        Room room = roomMapper.selectById(roomId);
        if (room == null)
        {
            throw new BusionessException("房间不存在");
        }
        User user = userMapper.selectById(userId);
        if (user == null)
        {
            throw new BusionessException("用户不存在");
        }
        Category category = categoryMapper.selectById(room.getCategoryId());
        //查该用户viplevel在该房型上的折扣；查不到按原价(1.0)
        BigDecimal discountRate = getVipDiscountRate(category, user.getViplevel());
        BigDecimal basePrice = null;
        BigDecimal priceAfterDiscount = BigDecimal.ZERO;
        if (category != null && category.getBasicPrice() != null)
        {
            basePrice = category.getBasicPrice();
            priceAfterDiscount = basePrice.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        }
        return RoomDetailResponseDTO.builder()
                .id(room.getId())
                .roomName(room.getName())
                .categoryId(room.getCategoryId())
                .categoryName(category.getName())
                .pictures(category != null && category.getPicture() != null ? category.getPicture() : java.util.Collections.emptyList())
                .basePrice(basePrice)
                .priceAfterDiscount(priceAfterDiscount)
                .description(category.getDescription())
                .build();
    }

    //查询某用户viplevel在某房型上的折扣幅度
    private BigDecimal getVipDiscountRate(Category category, Integer viplevel)
    {
        if (category == null)
        {
            return BigDecimal.ONE;
        }
        LambdaQueryWrapper<Discount> QueryWrapper= new LambdaQueryWrapper<Discount>();
        Discount discount = discountMapper.selectOne(
                QueryWrapper.eq(Discount::getViplevel, viplevel)
                        .eq(Discount::getCategoryId, category.getId()));

        return discount.getVipDiscount();
    }
    //查看所有房间（管理员）：返回房间id、名称、房型名、全部图片、基础价、优惠后价(无用户按原价)、描述
    public List<RoomDetailResponseDTO> listAllRooms()
    {
        List<Room> rooms = roomMapper.selectList(null);
        return rooms.stream().map(room ->
        {
            Category category = categoryMapper.selectById(room.getCategoryId());
            //无用户上下文，优惠后价格默认按原价（基础价）
            BigDecimal basePrice = category.getBasicPrice();
            return RoomDetailResponseDTO.builder()
                    .id(room.getId())
                    .roomName(room.getName())
                    .categoryId(room.getCategoryId())
                    .categoryName(category.getName())
                    .pictures(category.getPicture())
                    .basePrice(basePrice)
                    .priceAfterDiscount(basePrice)
                    .description(category.getDescription())
                    .build();
        }).collect(Collectors.toList());
    }

    //根据房间id获取自今日起该房间所有被占用日期（YYYY-MM-DD）
    public List<String> listOccupiedDates(String roomId)
    {
        Room room = roomMapper.selectById(roomId);
        if (room == null)
        {
            throw new BusionessException("房间不存在");
        }
        LocalDate today = LocalDate.now();
        List<RoomOccupation> occupations = roomOccupationMapper.selectList(
                new LambdaQueryWrapper<RoomOccupation>()
                        .eq(RoomOccupation::getRoomId, roomId)
                        .ge(RoomOccupation::getEndtime, today.atStartOfDay()));
        if (occupations == null || occupations.isEmpty())
        {
            return Collections.emptyList();
        }
        //占用区间为[starttime, endtime)，被占用的日历日期为起止两日期之间的每一天
        Set<LocalDate> occupiedDates = new TreeSet<>();
        for (RoomOccupation occupation : occupations)
        {
            LocalDate start = occupation.getStarttime().toLocalDate();
            LocalDate end = occupation.getEndtime().toLocalDate();
            for (LocalDate d = start; d.isBefore(end); d = d.plusDays(1))
            {
                if (!d.isBefore(today))
                {
                    occupiedDates.add(d);
                }
            }
        }
        return occupiedDates.stream().map(LocalDate::toString).collect(Collectors.toList());
    }
}