package com.example.demo.common;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> success(){
        Result<T> result = new Result<>();
        result.code = 200;
        result.msg = "操作成功！";
        result.data = null;
        return result;
    }

    public static <T> Result<T> success(T data){
        Result<T> result = new Result<>();
        result.code = 200;
        result.msg = "操作成功！";
        result.data = data;
        return result;
    }
    public static <T> Result<T> success(String msg,T data){
        Result<T> result = new Result<>();
        result.code = 200;
        result.msg = msg;
        result.data = data;
        return result;
    }
    public static <T> Result<T> success(int code, String msg,T data){
        Result<T> result = new Result<>();
        result.code = code;
        result.msg = msg;
        result.data = data;
        return result;
    }

    public static <T> Result<T> error(int code, String msg){
        Result<T> result = new Result<>();
        result.code = code;
        result.msg = msg;
        return result;
    }
}
