package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@TableName("comment")
public class Comment {
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;
    
    @TableField("author_id")
    private Long authorId;
    
    @TableField("article_id")
    @NotNull(message = "文章ID不能为空")
    private Long articleId;
    
    @TableField("parent_id")
    private Long parentId;
    
    @TableField("create_time")
    private Date createTime;

    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 500, message = "评论内容长度必须在1-500个字符之间")
    private String content;

    @TableField(exist = false)
    private List<Comment> children =  new ArrayList<>();
}
