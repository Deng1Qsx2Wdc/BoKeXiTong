package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.pojo.entity.Article;
import com.example.demo.pojo.entity.Follows;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FollowsMapper extends BaseMapper<Follows> {


}
