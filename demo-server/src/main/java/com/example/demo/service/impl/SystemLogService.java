package com.example.demo.service.impl;

import com.example.demo.mapper.SysOperLogMapper;
import com.example.demo.pojo.entity.SysOperLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SystemLogService {
    @Autowired
    private SysOperLogMapper sysOperLogMapper;
    @Async("asyncExecutor")
    public void saveSystemLog(SysOperLog sysOperLog){
        if(sysOperLog==null){
            return;
        }
        int n = sysOperLogMapper.insert(sysOperLog);
        if(n==0){
            return;
        }
    }
}
