package com.example.hotel.controller;

import com.example.hotel.DTO.response.VrResponseDTO;
import com.example.hotel.common.Result;
import com.example.hotel.service.VrService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class VrController {

    @Resource
    private VrService vrService;

    @GetMapping("/vr")
    public Result<VrResponseDTO> getVr(@RequestParam("categoryId") String categoryId) {
        VrResponseDTO data = vrService.getVrByCategoryId(categoryId);
        if (data == null) {
            return Result.error("404", "该房型尚未配置VR全景", null);
        }
        return Result.success(data);
    }
}