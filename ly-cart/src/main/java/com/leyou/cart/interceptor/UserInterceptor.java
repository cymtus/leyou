package com.leyou.cart.interceptor;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.utils.CookieUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-15 12:17
 **/
public class UserInterceptor implements HandlerInterceptor {

    private JwtProperties jwtProperties;

    public UserInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // 获取token
            String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());

            // 解析token，获取用户信息
            UserInfo user = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());

            // 保存用户信息
            tl.set(user);
            return true;
        } catch (Exception e) {
            response.setStatus(403);
        }
        return false;
    }

    public static UserInfo getUser(){
        return tl.get();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求处理完毕，清空User数据
        tl.remove();
    }
}
