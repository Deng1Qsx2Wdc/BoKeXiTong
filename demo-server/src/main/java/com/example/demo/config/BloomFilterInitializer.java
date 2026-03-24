package com.example.demo.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.mapper.ArticleMapper;
import com.example.demo.pojo.entity.Article;
import com.example.demo.utils.BloomFilterUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 布隆过滤器初始化器
 *
 * 应用启动时自动执行，将数据库中所有文章ID加载到布隆过滤器
 *
 * 实现方式：
 * 1. 实现CommandLineRunner接口，在Spring Boot应用启动后自动执行
 * 2. 查询数据库中所有文章ID
 * 3. 批量添加到布隆过滤器
 *
 * 注意事项：
 * - 只在应用启动时执行一次
 * - 如果数据量很大（百万级），建议分批加载
 * - 布隆过滤器数据存储在Redis中，重启应用不会丢失（除非Redis重启）
 */
@Slf4j
@Component
public class BloomFilterInitializer implements CommandLineRunner {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private BloomFilterUtil bloomFilterUtil;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化布隆过滤器...");

        try {
            // 查询数据库中所有文章ID
            QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("id"); // 只查询ID字段，减少内存占用
            List<Article> articles = articleMapper.selectList(queryWrapper);

            if (articles == null || articles.isEmpty()) {
                log.info("数据库中暂无文章，跳过布隆过滤器初始化");
                return;
            }

            // 提取所有文章ID
            List<Long> articleIds = articles.stream()
                    .map(Article::getId)
                    .collect(Collectors.toList());

            // 批量添加到布隆过滤器
            bloomFilterUtil.addBatch(articleIds);

            log.info("布隆过滤器初始化完成，共加载{}个文章ID", articleIds.size());
        } catch (Exception e) {
            log.error("布隆过滤器初始化失败", e);
            // 不抛出异常，避免影响应用启动
            // 即使布隆过滤器初始化失败，应用仍然可以正常运行（只是缓存穿透防护效果会差一些）
        }
    }
}
