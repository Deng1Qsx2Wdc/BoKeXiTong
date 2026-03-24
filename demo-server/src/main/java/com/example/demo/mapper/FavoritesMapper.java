package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.Favorites;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface  FavoritesMapper extends BaseMapper<Favorites> {
}
