package com.example.demo.common.enums;

/**
 * 互动状态枚举
 * 用于点赞、收藏、关注等互动功能的状态管理
 */
public enum InteractionStatus {
    
    /**
     * 未激活/已取消状态
     */
    INACTIVE(0, "未激活"),
    
    /**
     * 激活/已操作状态
     */
    ACTIVE(1, "已激活");
    
    private final Integer code;
    private final String description;
    
    InteractionStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据状态码获取枚举
     */
    public static InteractionStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (InteractionStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 判断是否为激活状态
     */
    public boolean isActive() {
        return this == ACTIVE;
    }
    
    /**
     * 判断是否为未激活状态
     */
    public boolean isInactive() {
        return this == INACTIVE;
    }
    
    /**
     * 切换状态
     */
    public InteractionStatus toggle() {
        return this == ACTIVE ? INACTIVE : ACTIVE;
    }
}
