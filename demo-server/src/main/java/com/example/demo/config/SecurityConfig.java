package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 1. 关闭 CSRF (暂时关闭，开发方便)
        http.csrf(csrf -> csrf.disable());
            // 2. 配置请求授权
        http.authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // 允许所有请求，不需要登录
        );
        return http.build();
    }
}
