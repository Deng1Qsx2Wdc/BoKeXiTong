package com.example.demo.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.time.Duration;

/**
 * AI API 配置类
 */
@Configuration
@ConfigurationProperties(prefix = "ai.api")
@Data
@Slf4j
public class AIConfig {

    /**
     * API Key
     */
    private String key;

    /**
     * API Base URL
     */
    private String baseUrl;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 超时时间（毫秒）
     */
    private Long timeout = 60000L;

    private OkHttpClient okHttpClient;

    /**
     * 创建 OkHttpClient Bean
     */
    @Bean
    public OkHttpClient okHttpClient() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofMillis(timeout))
                .readTimeout(Duration.ofMillis(timeout))
                .writeTimeout(Duration.ofMillis(timeout))
                .build();
        return okHttpClient;
    }

    /**
     * 清理 OkHttpClient 资源
     */
    @PreDestroy
    public void destroy() {
        if (okHttpClient != null) {
            log.info("正在关闭 OkHttpClient 资源...");
            okHttpClient.dispatcher().executorService().shutdown();
            okHttpClient.connectionPool().evictAll();
            log.info("OkHttpClient 资源已关闭");
        }
    }
}
