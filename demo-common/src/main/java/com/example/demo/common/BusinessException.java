package com.example.demo.common;

public class BusinessException extends RuntimeException {
    private final int code;
    // 构造函数：只传消息
    public BusinessException(String message) {
        super(message);
        this.code = 500;// 默认通用业务错误码
    }
    // 构造函数：传枚举
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
    // 构造函数：传状态码和消息
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    // 构造函数：传枚举和消息
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
