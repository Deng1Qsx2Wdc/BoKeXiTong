package com.example.demo.common.enums;

/**
 * 操作状态枚举
 * 用于操作日志记录操作是否成功
 */
public enum OperationStatus {
    
    /**
     * 操作失败
     */
    FAIL("0", "失败"),
    
    /**
     * 操作成功
     */
    SUCCESS("1", "成功");
    
    private final String code;
    private final String description;
    
    OperationStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据状态码获取枚举
     */
    public static OperationStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (OperationStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
