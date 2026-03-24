package com.example.demo.common.enums;

/**
 * 文章状态枚举
 * 统一管理文章的各种状态，避免魔法值
 */
public enum ArticleStatus {
    
    /**
     * 草稿状态 - 文章已保存但未发布
     */
    DRAFT(0, "草稿"),
    
    /**
     * 已发布状态 - 文章已公开发布
     */
    PUBLISHED(1, "已发布"),
    
    /**
     * 已下线状态 - 文章已被下线或删除
     */
    OFFLINE(2, "已下线");
    
    private final Integer code;
    private final String description;

    ArticleStatus(Integer code, String description) {
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
     * @param code 状态码
     * @return 对应的枚举，如果不存在返回null
     */
    public static ArticleStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ArticleStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 判断是否为草稿状态
     */
    public boolean isDraft() {
        return this == DRAFT;
    }
    
    /**
     * 判断是否为已发布状态
     */
    public boolean isPublished() {
        return this == PUBLISHED;
    }
    
    /**
     * 判断是否为已下线状态
     */
    public boolean isOffline() {
        return this == OFFLINE;
    }
}
