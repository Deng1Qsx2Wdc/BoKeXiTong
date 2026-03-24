package com.example.demo.mapper; // 包名

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.pojo.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// @Mapper：告诉 Spring，这是一个操作数据库的接口
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    // 啥都不用写！
    // 继承了 BaseMapper<Article> 后，你自动拥有了：
    // insert, delete, update, selectById, selectList 等几十个方法！
    Page<Article> getFollowingArticles(Page<Article> page,@Param("authorId") Long authorId);
}
