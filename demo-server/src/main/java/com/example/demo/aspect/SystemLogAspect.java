package com.example.demo.aspect;

import com.alibaba.fastjson.JSON;
import com.example.demo.annotation.SystemLog;
import com.example.demo.common.UserContext;
import com.example.demo.common.enums.OperationStatus;
import com.example.demo.pojo.entity.SysOperLog;
import com.example.demo.service.impl.SystemLogService;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import  java.lang.Object;
import java.lang.reflect.Method;
import java.util.Date;

@Slf4j
@Aspect
@Component
public class SystemLogAspect  {
    @Autowired
    private SystemLogService systemLogService;
    
    @Pointcut("@annotation(com.example.demo.annotation.SystemLog)")
    public void logPointCut() {}

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Long startTime = System.currentTimeMillis();
        Object result =null;
        String status = OperationStatus.SUCCESS.getCode(); // 使用枚举
        String msg = "";
        Long costTime;
        try{
            result= joinPoint.proceed();
        }catch (Exception e){
            status = OperationStatus.FAIL.getCode(); // 使用枚举
            msg = e.getMessage();
            log.error("操作失败: {}", msg, e);
            throw e;
        }finally {
            costTime= System.currentTimeMillis() - startTime;

            // 用于日志记录的JSON字符串
            String jsonResult = "";
            if(result != null){
                jsonResult = JSON.toJSONString(result);
            }

            // 传递JSON字符串给日志方法，而不是修改result变量
            systemLog(joinPoint, jsonResult, costTime, status, msg);
        }
        // 返回原始的Result对象，而不是JSON字符串
        return result;
    }
    private void systemLog(ProceedingJoinPoint joinPoint,Object result,Long time,String status,String msg) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        SystemLog annotation = method.getAnnotation(SystemLog.class);

        SysOperLog sysOperLog = new SysOperLog();
        Long authorId = UserContext.getThreadLocal();
        // 允许 authorId 为 null，因为某些操作可能不需要登录
        sysOperLog.setAuthorId(authorId);//作者ID
        sysOperLog.setModule(annotation.title());//模块名称,如文章模块
        sysOperLog.setBusinessType(annotation.businessName());//业务类型,如删除文章

        sysOperLog.setMethod(method.getName());//方法名
        sysOperLog.setStatus(status);//先默认
        sysOperLog.setCostTime(time);//执行耗时
        sysOperLog.setCreateTime(new Date());//日志创造时间
        sysOperLog.setErrorMsg(msg);//服务信息

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        if(requestAttributes == null){
            return;
        }
        HttpServletRequest  request = requestAttributes.getRequest();

        sysOperLog.setRequestType(request.getMethod());//请求方式

        sysOperLog.setRequestIp(request.getRemoteAddr());//IP
        sysOperLog.setRequestUrl(request.getRequestURL().toString());//URL
        sysOperLog.setRequestLocation(request.getLocalAddr());//地点
        sysOperLog.setRequestParams(request.getParameterMap().toString());//请求体
        sysOperLog.setJsonResult(result.toString());//返回参数/结果
        sysOperLog.setUserAgent(request.getHeader("User-Agent"));//User-Agent
        systemLogService.saveSystemLog(sysOperLog);
    }
}
