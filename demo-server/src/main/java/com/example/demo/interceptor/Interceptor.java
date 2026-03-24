package com.example.demo.interceptor;

import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.UserContext;
import com.example.demo.mapper.AuthorMapper;
import com.example.demo.pojo.entity.Author;
import com.example.demo.service.impl.TokenBlacklistService;
import com.example.demo.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.example.demo.common.ErrorCode.USER_NOT_FOUND;

/**
 * 拦截器
 */
@Component
public class Interceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    @Autowired
    private AuthorMapper authorMapper;
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    @Override// Spring规定：如果拦截器生效，则请求处理前调用
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object Handler)throws Exception{
        //重写方法，在请求正式被处理前执行。

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        //获取请求头的Authorization字段，将其赋值给token变量，令牌存在此字段

        if(token == null || !token.startsWith(BEARER_PREFIX)){
            throw new BusinessException(ErrorCode.TOKEN_MISSING);
        }
        //判断token变量是否存在且以‘Bearer ’开头，如果不是，则抛出token无效的异常。

        String actualToken = token.substring(BEARER_PREFIX.length());
        //跳过token变量的前七个字符，截取后面的、从第八个字符开始的、真正的token令牌。


        if(tokenBlacklistService.isBlacklisted(actualToken)){
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        //检查token是否在黑名单中。

        if(!"access".equals(JwtUtils.getTokenType(actualToken))){
            throw new BusinessException(ErrorCode.TOKEN_TYPE_ERROR);
        }
        //检查token类型是否为access，只有access才能通过。
        //只允许access token访问API

        Long id = JwtUtils.getUserIdFromToken(actualToken);
        //调用JWT工具类中的静态方法，解析并验证AccessToken后(验证签名和过期时间)，把存放在token令牌中的用户ID信息提取出来。

        Author author = authorMapper.selectById(id);
        //使用用户ID查数据库，目标是用户对象
        // 。
        if(author == null){
            throw new BusinessException(USER_NOT_FOUND);
        }
        //如果用户不存在就抛出‘用户不存在’的异常。

        UserContext.setThreadLocal(id);
        //将用户ID存入到线程本地。

        return true;
        //放行
    }
    @Override// Spring规定：如果拦截器生效，则在整个请求完成后调用（无论成功失败）
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        //重写方法，在请求结束后执行
        UserContext.removeThreadLocal();
        //清除线程本地中的数据，防止在复用线程的场景下造成内存泄露。
    }
}
