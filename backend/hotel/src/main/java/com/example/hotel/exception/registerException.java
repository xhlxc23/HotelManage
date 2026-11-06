package com.example.hotel.exception;

import lombok.Getter;

@Getter
public class registerException extends RuntimeException {
    //注册异常
    private  final String code;
    private  final String message;
    private  final Object data;
    public registerException(String message) {

        super(message);
        this.code="409";
        this.message=message;
        this.data=null;
    }
}
