package com.example.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hotel.DTO.command.CategoryCreateCommandDTO;
import com.example.hotel.DTO.command.DiscountCommandDTO;
import com.example.hotel.DTO.response.CategoryResponseDTO;
import com.example.hotel.entity.Category;
import com.example.hotel.entity.Discount;
import com.example.hotel.entity.Room;
import com.example.hotel.exception.BusionessException;
import com.example.hotel.mapper.CategoryMapper;
import com.example.hotel.mapper.DiscountMapper;
import com.example.hotel.mapper.RoomMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private RoomMapper roomMapper;

    @Resource
    private DiscountMapper discountMapper;

    /**
     * 新建房型
     */
    public void createCategory(CategoryCreateCommandDTO commandDTO)
    {
        //房型名唯一：已存在同名房型则不允许新增
        Long exists = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>().eq(Category::getName, commandDTO.getName()));
        if (exists != null && exists > 0)
        {
            throw new BusionessException("房型名已存在：" + commandDTO.getName());
        }
        Category category = Category.builder()
                .name(commandDTO.getName())
                .basicPrice(commandDTO.getBasicPrice())
                .maxnumber(0)
                .picture(commandDTO.getPicture())
                .description(commandDTO.getDescription())
                .build();
        categoryMapper.insert(category);
        //保存各VIP等级折扣
        saveDiscounts(category.getId(), commandDTO.getDiscounts());
    }

    public void requireCategoryExists(String categoryId)
    {
        if (categoryMapper.selectById(categoryId) == null)
        {
            throw new BusionessException("房型不存在");
        }
    }

    public List<CategoryResponseDTO> listAllCategories()
    {
        return categoryMapper.selectList(null).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    //修改房型信息：数据为空则不修改，有数据则修改
    public Void updateCategory(CategoryCreateCommandDTO commandDTO)
    {
        if (commandDTO.getId() == null || commandDTO.getId().isBlank())
        {
            throw new BusionessException("房型id不能为空");
        }
        Category category = categoryMapper.selectById(commandDTO.getId());
        if (category == null)
        {
            throw new BusionessException("房型不存在");
        }
        if (commandDTO.getName() != null && !commandDTO.getName().isBlank())
        {
            //改名时校验新房型名不与其它房型重复
            Long exists = categoryMapper.selectCount(
                    new LambdaQueryWrapper<Category>()
                            .eq(Category::getName, commandDTO.getName())
                            .ne(Category::getId, commandDTO.getId()));
            if (exists != null && exists > 0)
            {
                throw new BusionessException("房型名已存在：" + commandDTO.getName());
            }
            category.setName(commandDTO.getName());
        }
        category.setBasicPrice(commandDTO.getBasicPrice());
        category.setPicture(commandDTO.getPicture());
        if (commandDTO.getDescription() != null)
        {
            category.setDescription(commandDTO.getDescription());
        }
        categoryMapper.updateById(category);
        //修改：覆盖式更新各VIP等级折扣
        if (commandDTO.getDiscounts() != null)
        {
            discountMapper.delete(
                    new LambdaQueryWrapper<Discount>().eq(Discount::getCategoryId, commandDTO.getId()));
            saveDiscounts(commandDTO.getId(), commandDTO.getDiscounts());
        }
        return null;
    }

    //删除房型：存在房间引用时不允许删除
    public void deleteCategory(String categoryId)
    {
        //1. 校验房型存在
        Category category = categoryMapper.selectById(categoryId);
        if (category == null)
        {
            throw new BusionessException("房型不存在");
        }
        //2. 校验没有房间引用该房型
        Long roomCount = roomMapper.selectCount(
                new LambdaQueryWrapper<Room>().eq(Room::getCategoryId, categoryId));
        if (roomCount != null && roomCount > 0)
        {
            throw new BusionessException("该房型下存在房间，无法删除");
        }
        //3. 删除该房型关联的折扣配置
        discountMapper.delete(
                new LambdaQueryWrapper<Discount>().eq(Discount::getCategoryId, categoryId));
        //4. 删除房型
        categoryMapper.deleteById(categoryId);
    }

    //批量保存某房型各VIP等级折扣
    private void saveDiscounts(String categoryId, List<DiscountCommandDTO> discounts)
    {
        if (discounts == null)
        {
            return;
        }
        discounts.forEach(d ->
                discountMapper.insert(Discount.builder()
                        .viplevel(d.getViplevel())
                        .categoryId(categoryId)
                        .vipDiscount(d.getVipDiscount())
                        .build()));
    }

    //房型持实体转响应DTO，带上该房型的各VIP等级折扣
    private CategoryResponseDTO toResponse(Category category)
    {
        List<DiscountCommandDTO> discounts = discountMapper.selectList(
                        new LambdaQueryWrapper<Discount>().eq(Discount::getCategoryId, category.getId()))
                .stream()
                .map(d -> DiscountCommandDTO.builder()
                        .viplevel(d.getViplevel())
                        .vipDiscount(d.getVipDiscount())
                        .build())
                .collect(Collectors.toList());
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .basicPrice(category.getBasicPrice())
                .discounts(discounts)
                .picture(category.getPicture())
                .description(category.getDescription())
                .build();
    }
}