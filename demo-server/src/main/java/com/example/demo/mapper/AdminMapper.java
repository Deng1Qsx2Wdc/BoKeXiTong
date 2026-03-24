package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.Admin;
import org.apache.ibatis.annotations.Mapper;


@Mapper // 👈 告诉 Spring Boot 这是一个操作数据库的家伙
public interface AdminMapper  extends BaseMapper<Admin> {
}