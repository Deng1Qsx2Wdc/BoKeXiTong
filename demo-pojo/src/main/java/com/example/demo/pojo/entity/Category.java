package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;

@Data
@TableName("category")
public class Category {
    @TableId(type = IdType.ASSIGN_ID)
    @NotNull(message = "分类ID不能为空", groups = {ValidationGroups.Delete.class, ValidationGroups.Update.class})
    private Long id;

    @NotBlank(message = "分类名称不能为空", groups = {Default.class, ValidationGroups.Update.class})
    @Size(min = 1, max = 50, message = "分类名称长度必须在1-50个字符之间", groups = {Default.class, ValidationGroups.Update.class})
    private String name;
}
