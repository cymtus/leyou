package com.leyou.filter;

import com.leyou.auth.utils.JwtUtils;
import com.leyou.config.FilterProperties;
import com.leyou.config.JwtProperties;
import com.leyou.utils.CookieUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import io.jsonwebtoken.Jwt;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-15 10:07
 **/
@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class LoginFilter extends ZuulFilter {

    @Autowired
    private JwtProperties prop;

    @Autowired
    private FilterProperties filterProperties;

    private static final Logger logger = LoggerFactory.getLogger(LoginFilter.class);

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 5;
    }

    @Override
    public boolean shouldFilter() {
        // 获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String path = request.getRequestURI();// /api/user/check/huge/1
        // 判断请求的路径是否需要拦截
        return !isAllowPath(path);
    }

    private boolean isAllowPath(String path) {
        boolean boo = false;
        List<String> list = this.filterProperties.getAllowPaths();
        for (String allowPath : list) {
            if(path.startsWith(allowPath)){
                boo = true;
                break;
            }
        }
        return boo;
    }

    @Override
    public Object run() throws ZuulException {
        // 获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        // 获取token
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());

        try {
            if (StringUtils.isNotBlank(token)) {
                // 解析token
                JwtUtils.getInfoFromToken(token, prop.getPublicKey());
                // 解析通过，啥都不做
                return null;
            }
        } catch (Exception e){
            logger.error("用户信息认证失败", e);
        }
        // 拦截，不允许继续向下
        ctx.setSendZuulResponse(false);
        ctx.setResponseStatusCode(403);
        return null;
    }
}
