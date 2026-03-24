package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("thumbs_up")
public class Thumbs_up {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("author_id")
    private Long authorId;

    @TableField("article_id")
    private Long articleId;

    @TableField(value = "thumbs_up_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date thumbsUpTime;

    @TableField("status")
    private Integer status;
}
