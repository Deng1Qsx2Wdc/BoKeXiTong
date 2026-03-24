package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.config.BCrypt;
import com.example.demo.config.SecurityConfig;
import com.example.demo.pojo.entity.Admin;
import com.example.demo.mapper.AdminMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private BCrypt bCrypt;
    @Transactional(readOnly = true)
    public Admin LoginAdmin(String username,String password) {
        QueryWrapper<Admin> queryAdmin = new QueryWrapper<>();
        queryAdmin.eq("username",username);
        Admin admin = adminMapper.selectOne(queryAdmin);
        if(admin==null){
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        Boolean aBoolean = bCrypt.passwordEncoder().matches(password,admin.getPassword());
        if(!aBoolean){
            throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR);
        }
        return admin;
    }

}
