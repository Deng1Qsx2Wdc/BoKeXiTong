package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@TableName("author")
public class Author extends Admin {
    @TableId(type = IdType.ASSIGN_ID)
//    @JsonSerialize(using = ToStringSerializer.class)
    @NotNull(message = "用户ID不能为空", groups = {ValidationGroups.Delete.class, ValidationGroups.Update.class})
    private Long id;

    @NotBlank(message = "用户名不能为空", groups = {ValidationGroups.Update.class})
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间", groups = {ValidationGroups.Update.class})
    @Pattern(regexp = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$", message = "用户名只能包含字母、数字、下划线和中文", groups = {ValidationGroups.Update.class})
    private String username;

    @NotBlank(message = "密码不能为空", groups = {ValidationGroups.Register.class, ValidationGroups.Login.class})
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间", groups = {ValidationGroups.Register.class, ValidationGroups.Update.class})
    private String password;

    @TableField(exist = false)
    private String token;
}
