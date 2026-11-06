package com.example.hotel.exception;

import lombok.Getter;

@Getter
public class adminloginException extends RuntimeException {

    private  final String code;
    private  final String message;
    private  final Object data;

    public adminloginException(String message)
    {
        super(message);
        this.code="404";
        this.message=message;
        this.data=null;
    }
}
