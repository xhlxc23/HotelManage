package com.example.hotel.common;

public enum ResultCode {
    SUCCESS("200","success"),
    PARAM_ERROR("-1","param error"),
    USER_NOT_FOUND("404","user not found"),
    ERROR("-1","error");

    private String code;
    private String msg;
    ResultCode(String code,String msg)
    {
        this.code=code;
        this.msg=msg;
    }

    public String getcode()
    {
        return code;
    }
    public String getMsg()
    {
        return msg;
    }
}
