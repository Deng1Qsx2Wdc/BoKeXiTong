package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.constants.CacheConstants;
import com.example.demo.pojo.entity.Article;
import com.example.demo.pojo.entity.Cate_Ari_Total;
import com.example.demo.pojo.entity.Category;
import com.example.demo.pojo.entity.Dashboard;
import com.example.demo.mapper.ArticleMapper;
import com.example.demo.mapper.AuthorMapper;
import com.example.demo.mapper.CategoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DashboardService {//后台总览
    private static final Long SevenDays_S = 7 * 24 * 60 * 60 * 1000L;
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private AuthorMapper authorMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    private static final String GetCategory_Article_Total = "getcategory_article_total:list";

    @Cacheable(value = CacheConstants.DASHBOARD_COUNT)
    public Dashboard getCount() {
        log.info("获取Dashboard统计数据");
        
        Dashboard dashboard = new Dashboard();
        dashboard.setArticleTotal(articleMapper.selectCount(null));
        dashboard.setCategoryTotal(categoryMapper.selectCount(null));
        dashboard.setAuthorTotal(authorMapper.selectCount(null));

        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        Date now = new Date();
        queryWrapper.ge("create_time", now.getTime()-SevenDays_S);//七天之内的文章总数
        dashboard.setLatest_seven_days_article_total(articleMapper.selectCount(queryWrapper));//最新七天文章
        if(dashboard == null){
            log.error("获取Dashboard统计数据失败");
            throw new BusinessException(ErrorCode.DATABASE_QUERY_FAILED);
        }
        log.info("获取Dashboard统计数据成功，文章总数: {}，分类总数: {}，用户总数: {}，最近7天文章数: {}", 
                dashboard.getArticleTotal(), dashboard.getCategoryTotal(), 
                dashboard.getAuthorTotal(), dashboard.getLatest_seven_days_article_total());
        return dashboard;
    }


    @Cacheable(value = CacheConstants.CATEGORY_ARTICLE_TOTAL,key = "categoryId=#categoryId")
    public List<Cate_Ari_Total> getCategory_article_total(Long categoryId) {//得到某类型下的文章总数
        log.info("获取分类文章统计，分类ID: {}", categoryId);

        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.groupBy("category_id");//按照博客类型进行分组
        queryWrapper.select("category_id","count(*) as count");//统计每个类型下面的文章总数
        List<Map<String,Object>> list = articleMapper.selectMaps(queryWrapper);//查找，返回字典列表

        List<Category> categories = categoryMapper.selectList(null);//目的是:方便得到某个博客类型ID下面的类型名
        Map<Long,Category> categoryMap = categories.stream().collect(Collectors.toMap(Category::getId,c->c));

        List<Cate_Ari_Total> cate_ari_totalList = new ArrayList<>();//容器
        for(Map<String,Object> map : list){
            Cate_Ari_Total cate_Ari_Total = new Cate_Ari_Total();//临时容器

            Long id = (Long)map.get("category_id");
            cate_Ari_Total.setId(id);

            Integer count = (Integer)map.get("count");
            cate_Ari_Total.setCount(count);

            Category category = categoryMap.get(id);
            cate_Ari_Total.setName(category.getName());
            cate_ari_totalList.add(cate_Ari_Total);
        }
        if(cate_ari_totalList == null){
            log.error("获取分类文章统计失败");
            throw new BusinessException(ErrorCode.DATABASE_QUERY_FAILED);
        }

        log.info("获取分类文章统计成功，共{}个分类", cate_ari_totalList.size());
        return cate_ari_totalList;
    }
}
