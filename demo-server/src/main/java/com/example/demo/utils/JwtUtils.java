package com.example.demo.utils;

import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.UserContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    //拿到配置文件中的jwt.secret常量的值，并将其赋值给secret变量。密钥。
    @Value("${jwt.secret}")
    private String secret;
    //拿到配置文件中的jwt.expiration常量的值，并将其赋值给expiration变量。有效时长。
    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    private static Key key;

    private static long ACCESS_EXPIRATION_TIME;

    private static long REFRESH_EXPIRATION_TIME;

    private static final String REFRESH = "refresh";

    @PostConstruct
    public void init(){
        //因为HMAC-SHA的加密对象是字节流、而不是字符串，所以需要先将字符串类型的密钥转化为字节流。
        key = Keys.hmacShaKeyFor(secret.getBytes());
        ACCESS_EXPIRATION_TIME =accessTokenExpiration;
        REFRESH_EXPIRATION_TIME = refreshTokenExpiration;
    }

    public static String createAccessToken(String adminId) {

        //获取当前时间，即签发时间
        Date now = new Date();
        //设置令牌的过期时间，即 过期时间 = 当前时间 + 过期时长。
        Date expiration = new Date(now.getTime()+ACCESS_EXPIRATION_TIME);

        return Jwts.builder()
                //拿到一个JWT的构建器。

                .setSubject(adminId)
                //将用户ID作为主体信息放到载荷的sub字段。

                .claim("type","access")
                //添加一个自定义字段，标识当前是AccessToken

                .setIssuedAt(now)
                //设置签发时间

                .setExpiration(expiration)
                //设置过期时间

                .signWith(key)
                //装填加密密钥并正式开始对设置好的信息进行加密签名，结果是一个JWT对象。

                .compact();
                //将一个JWT对象序列化为字符串格式。

    }
    public static String createRefreshToken(String adminId) {

        //获取当前时间，即签发时间
        Date now = new Date();
        //设置令牌的过期时间，即 过期时间 = 当前时间 + 过期时长。
        Date expiration = new Date(now.getTime()+REFRESH_EXPIRATION_TIME);

        return Jwts.builder()
                //拿到一个JWT的构建器。

                .setSubject(adminId)
                //将用户ID作为主体信息放到载荷的sub字段。

                .claim("type","refresh")
                //添加一个自定义字段，标识当前是RefreshToken

                .setIssuedAt(now)
                //设置签发时间

                .setExpiration(expiration)
                //设置过期时间

                .signWith(key)
                //装填加密密钥并正式开始对设置好的信息进行加密签名，结果是一个JWT对象。

                .compact();
        //将一个JWT对象序列化为字符串格式。

    }
    public static Long getUserIdFromToken(String token){
        try{
             Claims claims = Jwts.parserBuilder()
                     //创建一个JWT解析器的构建器。

                    .setSigningKey(key)
                     //设置用于验证签名的密钥，即加密密钥。
                    .build()
                     //正式开始构建一个JWT解析器。

                    .parseClaimsJws(token)
                     //开始解析并验证token令牌，验证签名和过期时间，确保令牌有效。

                     .getBody();
                    //取出令牌中的载荷部分。

            return Long.valueOf(claims.getSubject());
            //取出放在令牌的载荷部分的sub字段的用户ID，并将其转化为Long类型。
        }catch (Exception e){
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }
    public static String getTokenType(String token){
        try{
            Claims claims = Jwts.parserBuilder()
                    //创建一个JWT解析器的构建器。

                    .setSigningKey(key)
                    //设置用于验证签名的密钥，即加密密钥。

                    .build()
                    //正式开始构建一个JWT解析器。

                    .parseClaimsJws(token)
                    //开始解析并验证token令牌，验证签名和过期时间，确保令牌有效。

                    .getBody();
            //取出令牌中的载荷部分。

            return claims.get("type", String.class);
            //取出放在令牌的载荷部分的自定义type字段的token类型。
        }catch (Exception e){
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }

    public static Long getRemainingTime(String token){
        try{
            Claims claims = Jwts.parserBuilder()
                    //创建一个JWT解析器的构建器。

                    .setSigningKey(key)
                    //设置用于验证签名的密钥，即加密密钥。

                    .build()
                    //正式开始构建一个JWT解析器。

                    .parseClaimsJws(token)
                    //开始解析并验证token令牌，验证签名和过期时间，确保令牌有效。

                    .getBody();
            //取出令牌中的载荷部分。

            Date expiration = claims.getExpiration();
            Long remaining = expiration.getTime() - System.currentTimeMillis();
            return remaining;
        }catch (Exception e){
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }

    public static Boolean checkTokenType(String token){
        //检查Token类型是否为Refresh
        try{
            Claims claims = Jwts.parserBuilder()
                    //创建一个JWT解析器的构建器。

                    .setSigningKey(key)
                    //设置用于验证签名的密钥，即加密密钥。

                    .build()
                    //正式开始构建一个JWT解析器。

                    .parseClaimsJws(token)
                    //开始解析并验证token令牌，验证签名和过期时间，确保令牌有效。

                    .getBody();
            //取出令牌中的载荷部分。

            String Type = claims.get("type", String.class);

            if(!Type.equals(REFRESH)){
                return false;
            }
            return true;
        }catch (Exception e){
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }
    public static Boolean checkTokenTime(String token){
        //检查Token是否过期
        try{
            Jwts.parserBuilder()
                    //创建一个JWT解析器的构建器。

                    .setSigningKey(key)
                    //设置用于验证签名的密钥，即加密密钥。

                    .build()
                    //正式开始构建一个JWT解析器。

                    .parseClaimsJws(token);
                    //开始解析并验证token令牌，验证签名和过期时间，确保令牌有效。
            return true;
        }catch (Exception e){
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }
}
