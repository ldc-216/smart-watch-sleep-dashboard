package com.sleepdashboard.common;

import lombok.Data;

/**
 * 统一响应结果包装，所有 Controller 返回这个类型，前端拿到的 JSON 结构统一：
 * { "code": 200, "message": "ok", "data": ... }
 */
@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("ok");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
