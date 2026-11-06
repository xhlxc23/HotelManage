package com.example.hotel.controller;

import com.example.hotel.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class Test {
    @GetMapping("/test")
    public Result<String> tset(){

        return Result.success("hello world");
    }
}
