package com.example.hotel.common;

import lombok.Data;

@Data
public class Result<T> {
    private String code;
    private String msg;
    private T data;

    public static <T> Result<T> success ()
    {
        Result<T> result=new Result<>();
        result.setCode(ResultCode.SUCCESS.getcode());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        return result;
    }

    public static <T> Result<T> success (T data)
    {
        Result<T> result=new Result<>();
        result.setCode(ResultCode.SUCCESS.getcode());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error ()
    {
        Result<T> result=new Result<>();
        result.setCode("-1");
        result.setMsg("error");
        return result;
    }

    public static <T> Result<T> error (String code,String msg,T data)
    {
        Result<T> result=new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}
