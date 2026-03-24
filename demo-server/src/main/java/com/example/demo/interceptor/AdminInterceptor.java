package com.example.demo.interceptor;

import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.UserContext;
import com.example.demo.mapper.AdminMapper;
import com.example.demo.pojo.entity.Admin;
import com.example.demo.service.impl.TokenBlacklistService;
import com.example.demo.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AdminInterceptor extends Interceptor {
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(ErrorCode.TOKEN_MISSING);
        }

        String actualToken = token.substring(BEARER_PREFIX.length());
        if (tokenBlacklistService.isBlacklisted(actualToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        if (!"access".equals(JwtUtils.getTokenType(actualToken))) {
            throw new BusinessException(ErrorCode.TOKEN_TYPE_ERROR);
        }

        Long adminId = JwtUtils.getUserIdFromToken(actualToken);
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        UserContext.setThreadLocal(adminId);
        return true;
    }
}
