package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 操作日志记录表 sys_oper_log
 */
@Data
@TableName("sys_oper_log") // 映射数据库表名
public class SysOperLog {

    /** 日志主键 ID */
    @TableId(value = "oper_id", type = IdType.ASSIGN_ID) // 对应数据库 oper_id
    private Long id;

    /** 操作人员ID / 作者ID */
    @TableField("user_id")
    private Long authorId;

    /** 模块名称 (例如：订单模块) */
    @TableField("title") // 对应数据库 title 字段
    private String module;

    /** 业务类型 (0其它 1新增 2修改 3删除) */
    @TableField("business_type")
    private String businessType;

    /** 方法名称 (例如：com.example.OrderController.create) */
    @TableField("method")
    private String method; // 对应你的 ClassName + Method

    /** 请求方式 (GET/POST) */
    @TableField("request_method")
    private String requestType;

    /** 操作状态 (0正常 1异常) */
    @TableField("status")
    private String status;

    /** 错误消息 (如果失败了) */
    @TableField("error_msg")
    private String errorMsg;

    /** 消耗时间 (毫秒) - 建议用 Long 类型方便计算 */
    @TableField("cost_time")
    private Long costTime;

    /** 操作时间 - 建议用 Date 类型 */
    @TableField("oper_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 返回前端时自动格式化
    private Date createTime;

    // --- 下面是请求环境信息 ---

    /** 请求 URL */
    @TableField("oper_url")
    private String requestUrl;

    /** 主机 IP 地址 */
    @TableField("oper_ip")
    private String requestIp;

    /** 操作地点 (例如：北京) */
    @TableField("oper_location")
    private String requestLocation;

    /** 请求参数 (JSON格式) */
    @TableField("oper_param")
    private String requestParams;

    /** 返回参数 (JSON格式) */
    @TableField("json_result")
    private String jsonResult;

    /** 浏览器标识 (User-Agent) */
    @TableField("user_agent")
    private String userAgent;
}