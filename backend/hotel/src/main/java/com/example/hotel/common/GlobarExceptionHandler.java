package com.example.hotel.common;

import com.example.hotel.exception.BusionessException;
import com.example.hotel.exception.adminloginException;
import com.example.hotel.exception.registerException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobarExceptionHandler {
    //处理参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handlerException(MethodArgumentNotValidException e)
    {
        String msg =  e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(","));
        return Result.error(ResultCode.PARAM_ERROR.getcode(),msg,null);
    }
    //处理用户登录异常
    @ExceptionHandler(BusionessException.class)
    public Result<?> handlerException(BusionessException e)
    {
        //如果业务异常有数据，返回数据
        if(e.getData()!=null)
        {
            return Result.error(ResultCode.ERROR.getcode(),e.getMessage(),e.getData());
        }
        //如果没有业务异常有数据，返回null
        return Result.error(ResultCode.USER_NOT_FOUND.getcode(),e.getMessage(),null);
    }
    //处理注册异常
    @ExceptionHandler(registerException.class)
    public Result<?> handlerException(registerException e)
    {
        if(e.getData()!=null)
        {
            return Result.error(e.getCode(),e.getMessage(),e.getData());
        }
        //如果没有业务异常有数据，返回null
        return Result.error(e.getCode(),e.getMessage(),null);
    }
    //处理管理员登录异常
    @ExceptionHandler(adminloginException.class)
    public Result<?> handlerException(adminloginException e)
    {
        if(e.getData()!=null)
        {
            return Result.error(e.getCode(),e.getMessage(),e.getData());
        }
        //如果没有业务异常有数据，返回null
        return Result.error(e.getCode(),e.getMessage(),null);
    }

}