package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
@Data
public class ArticleQuery {
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    private Integer pageSize;

    private Integer pageNum;

    private Long authorId;

    private String keyword;

    private Long categoryId;

    private Integer status;

    private Date startTime;

    private Date endTime;

    public String StringBuilder(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("articleQuery::");
        appendIfNotNull(stringBuilder,"PageSize",pageSize);
        appendIfNotNull(stringBuilder,"PageNum",pageNum);
        appendIfNotNull(stringBuilder,"AuthorId",authorId);
        appendIfNotNull(stringBuilder,"Keyword",keyword);
        appendIfNotNull(stringBuilder,"CategoryId",categoryId);
        appendIfNotNull(stringBuilder,"Status",status);
        appendIfNotNull(stringBuilder,"UpdateTime",startTime);
        appendIfNotNull(stringBuilder,"CreateTime",endTime);
        return stringBuilder.toString();
    }
    private void appendIfNotNull(StringBuilder stringBuilder,String fieldName,Object value){
        if(value!=null){
            stringBuilder.append(",").append(fieldName).append("=").append(value);
        }
    }

}