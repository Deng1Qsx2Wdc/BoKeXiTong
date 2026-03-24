package com.example.demo.config;

import com.example.demo.interceptor.AdminInterceptor;
import com.example.demo.interceptor.ArticleInterceptor;
import com.example.demo.interceptor.AuthorInterceptor;
import com.example.demo.interceptor.CategoryInterceptor;
import com.example.demo.interceptor.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Autowired
    private AuthorInterceptor authorInterceptor;

    @Autowired
    private ArticleInterceptor articleInterceptor;

    @Autowired
    private CategoryInterceptor categoryInterceptor;

    @Autowired
    private Interceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login");

        registry.addInterceptor(authorInterceptor)
                .addPathPatterns("/author/**")
                .excludePathPatterns(
                        "/author/login",
                        "/author/register",
                        "/author/query",
                        "/author/queryone",
                        "/author/querybyid"
                );

        registry.addInterceptor(articleInterceptor)
                .addPathPatterns("/article/**")
                .excludePathPatterns("/article/query", "/article/queryone");

        registry.addInterceptor(categoryInterceptor)
                .addPathPatterns("/category/**")
                .excludePathPatterns("/category/query", "/category/queryone");

        registry.addInterceptor(interceptor)
                .addPathPatterns("/comment/**", "/follows/**", "/favorite/**", "/thumbs_up/**")
                .excludePathPatterns("/comment/getCommentList");
    }
}
