package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("favorites")
public class Favorites {
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("author_id")
    private Long authorId;

    @TableField("article_id")
    private Long articleId;

    @TableField(value = "favorites_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date favoritesTime;

    @TableField("status")
    private Integer status;

}
