package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data  // Lombok 注解：自动生成 Getter, Setter, toString, equals 等方法
@TableName("article")
public class Article {

    @TableId(value = "id",type = IdType.ASSIGN_ID)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @TableField(value = "category_id")
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotBlank(message = "文章标题不能为空")
    @Size(min = 1, max = 200, message = "文章标题长度必须在1-200个字符之间")
    private String title;
    
    @NotBlank(message = "文章内容不能为空")
    @Size(min = 1, max = 50000, message = "文章内容长度必须在1-50000个字符之间")
    private String content;
    
    private Integer status;

    @TableField(value = "author_id")
    private Long authorId;

    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(value = "create_time")
    private Date createTime;

    @TableField(value = "thumbs_up")
    private Integer thumbsUp;

    @TableField("favorites")
    private Integer favorites;
}