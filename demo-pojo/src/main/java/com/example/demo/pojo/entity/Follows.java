package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("follows")
public class Follows {
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("author_id")
    private Long authorId;

    @TableField("target_id")
    private Long targetId;

    @TableField("follow_time")
    private Date followTime;

    @TableField("status")
    private Integer status;
}
