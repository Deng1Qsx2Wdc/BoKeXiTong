package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.constants.CacheConstants;
import com.example.demo.pojo.entity.ArticleQuery;
import com.example.demo.pojo.entity.Category;
import com.example.demo.mapper.CategoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.lang.String;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;


    @CacheEvict(value = CacheConstants.CATEGORY, allEntries = true)
    @Transactional
    public void InsertCategory(Category category){
        log.info("添加分类，分类名称: {}", category.getName());
        
        int n = categoryMapper.insert(category);
        if(n<=0){
            log.error("添加分类失败，分类名称: {}", category.getName());
            throw new BusinessException(ErrorCode.CATEGORY_INSERT_FAILED);
        }
        log.info("添加分类成功，分类ID: {}", category.getId());
    }
    
    @CacheEvict(value = CacheConstants.CATEGORY, allEntries = true)
    @Transactional
    public void DeleteCategory(Category category){
        log.info("删除分类，分类ID: {}", category.getId());

        int n = categoryMapper.deleteById(category.getId());
        if(n<=0){
            log.error("删除分类失败，分类ID: {}", category.getId());
            throw new BusinessException(ErrorCode.CATEGORY_DELETE_FAILED);
        }
        log.info("删除分类成功，分类ID: {}", category.getId());
    }
    
    @Cacheable(value = CacheConstants.CATEGORY,key = "#articleQuery.StringBuilder()")
    public Page QueryCategory(ArticleQuery articleQuery){
        log.info("查询分类列表，页码: {}，页大小: {}", articleQuery.getPageNum(), articleQuery.getPageSize());
        
        Page<Category> page = new Page<>(articleQuery.getPageNum(),articleQuery.getPageSize());
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(articleQuery.getKeyword()),"name",articleQuery.getKeyword());
        queryWrapper.eq(articleQuery.getCategoryId() != null,"id",articleQuery.getCategoryId());
        queryWrapper.orderByAsc("name");
        categoryMapper.selectPage(page,queryWrapper);
        if(page == null){
            log.error("查询分类列表失败");
            throw new BusinessException(ErrorCode.CATEGORY_QUERY_FAILED);
        }
        log.info("查询分类列表成功，共{}条记录", page.getTotal());
        return page;
    }
    
    @Cacheable(value = CacheConstants.CATEGORY,key = "#id")
    @Transactional(readOnly = true)
    public Category QueryCategoryOne(Long id){
        log.info("查询单个分类，分类ID: {}", id);
        
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",id);
        Category category = categoryMapper.selectOne(queryWrapper);
        if(category == null){
            log.warn("查询分类失败，分类不存在，ID: {}", id);
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        log.info("查询分类成功，分类名称: {}", category.getName());
        return category;
    }

    @CacheEvict(value = CacheConstants.CATEGORY, allEntries = true)
    @Transactional
    public void UpdateCategory(Category category){
        log.info("更新分类，分类ID: {}", category.getId());
        
        int n= categoryMapper.updateById(category);
        if(n == 0){
            log.error("更新分类失败，分类ID: {}", category.getId());
            throw new BusinessException(ErrorCode.CATEGORY_UPDATE_FAILED);
        }
        log.info("更新分类成功，分类ID: {}", category.getId());
    }
}
