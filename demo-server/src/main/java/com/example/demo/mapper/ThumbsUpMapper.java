package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.Follows;
import com.example.demo.pojo.entity.Thumbs_up;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ThumbsUpMapper extends BaseMapper<Thumbs_up> {


}
