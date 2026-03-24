package com.example.demo.common;

/**
 * 错误码枚举
 * 统一管理系统中的所有错误码
 */
public enum ErrorCode {
    
    // ========== 通用错误码 1xxx ==========
    SUCCESS(200, "操作成功"),
    SYSTEM_ERROR(500, "系统繁忙，请稍后再试"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    
    // ========== 用户相关错误码 10xxx ==========
    USER_NOT_FOUND(10001, "用户不存在"),
    USER_ALREADY_EXISTS(10002, "用户名已存在"),
    USER_PASSWORD_ERROR(10003, "账号或密码错误"),
    USER_DISABLED(10004, "用户已被禁用"),
    USER_REGISTER_FAILED(10005, "用户注册失败"),
    USER_UPDATE_FAILED(10006, "用户信息更新失败"),
    USER_DELETE_FAILED(10007, "用户删除失败"),
    USER_NOT_LOGIN(10008, "用户没有登录"),

    // ========== Token相关错误码 11xxx ==========
    TOKEN_INVALID(11001, "Token无效或已过期"),
    TOKEN_MISSING(11002, "未提供Token"),
    TOKEN_EXPIRED(11003, "Token已过期"),
    TOKEN_FORMAT_ERROR(11004, "Token格式错误"),
    TOKEN_TYPE_ERROR(11004, "Token类型错误"),

    // ========== 文章相关错误码 20xxx ==========
    ARTICLE_NOT_FOUND(20001, "文章不存在"),
    ARTICLE_TITLE_EMPTY(20002, "文章标题不能为空"),
    ARTICLE_CONTENT_EMPTY(20003, "文章内容不能为空"),
    ARTICLE_INSERT_FAILED(20004, "文章添加失败"),
    ARTICLE_UPDATE_FAILED(20005, "文章更新失败"),
    ARTICLE_DELETE_FAILED(20006, "文章删除失败"),
    ARTICLE_PERMISSION_DENIED(20007, "无权操作他人文章"),
    ARTICLE_PUBLISH_FAILED(20008, "文章发布失败"),
    ARTICLE_OFFLINE_FAILED(20009, "文章下线失败"),
    ARTICLE_SAVE_DRAFT_FAILED(20010, "保存草稿失败"),
    
    // ========== 分类相关错误码 21xxx ==========
    CATEGORY_NOT_FOUND(21001, "分类不存在"),
    CATEGORY_NAME_EMPTY(21002, "分类名称不能为空"),
    CATEGORY_INSERT_FAILED(21003, "分类添加失败"),
    CATEGORY_UPDATE_FAILED(21004, "分类更新失败"),
    CATEGORY_DELETE_FAILED(21005, "分类删除失败"),
    CATEGORY_QUERY_FAILED(21006, "分类查询失败"),
    
    // ========== 评论相关错误码 22xxx ==========
    COMMENT_NOT_FOUND(22001, "评论不存在"),
    COMMENT_CONTENT_EMPTY(22002, "评论内容不能为空"),
    COMMENT_INSERT_FAILED(22003, "评论发布失败"),
    COMMENT_DELETE_FAILED(22004, "评论删除失败"),
    
    // ========== 点赞相关错误码 23xxx ==========
    THUMBS_UP_FAILED(23001, "点赞失败"),
    THUMBS_UP_CANCEL_FAILED(23002, "取消点赞失败"),
    
    // ========== 收藏相关错误码 24xxx ==========
    FAVORITES_FAILED(24001, "收藏失败"),
    FAVORITES_CANCEL_FAILED(24002, "取消收藏失败"),
    
    // ========== 关注相关错误码 25xxx ==========
    FOLLOW_FAILED(25001, "关注失败"),
    FOLLOW_CANCEL_FAILED(25002, "取消关注失败"),
    FOLLOW_ALREADY_EXISTS(25003, "已关注该用户"),
    FOLLOW_NOT_FOUND(25004, "未关注该用户"),
    
    // ========== 缓存相关错误码 30xxx ==========
    CACHE_LOCK_FAILED(30001, "获取锁失败，系统繁忙"),
    CACHE_ERROR(30002, "缓存操作失败"),
    
    // ========== 数据库相关错误码 31xxx ==========
    DATABASE_ERROR(31001, "数据库操作失败"),
    DATABASE_QUERY_FAILED(31002, "数据查询失败"),
    DATABASE_INSERT_FAILED(31003, "数据插入失败"),
    DATABASE_UPDATE_FAILED(31004, "数据更新失败"),
    DATABASE_DELETE_FAILED(31005, "数据删除失败");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    /**
     * 根据错误码获取枚举
     * @param code 错误码
     * @return 对应的枚举，如果不存在返回null
     */
    public static ErrorCode fromCode(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        return null;
    }
}
