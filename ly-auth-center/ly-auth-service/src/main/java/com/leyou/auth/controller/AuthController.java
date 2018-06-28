package com.leyou.auth.controller;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-14 12:19
 **/
@RestController
public class AuthController {

    @Value("${ly.jwt.cookieName}")
    private String cookieName;

    @Autowired
    private AuthService authService;

    @PostMapping("/accredit")
    public ResponseEntity<Void> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest req, HttpServletResponse resp) {
        // 登录校验生成token
        String token = this.authService.login(username, password);
        if (StringUtils.isBlank(token)) {
            // 登录失败
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // 把token写入cookie
        CookieUtils.setCookie(req, resp, cookieName, token,
                null, null, true);
        return ResponseEntity.ok().build();
    }

    /**
     * 登录校验
     * @param token
     * @return
     */
    @GetMapping("/verify")
    public ResponseEntity<UserInfo> verify(@CookieValue("LY_TOKEN") String token,
                                           HttpServletRequest req, HttpServletResponse resp) {
        try {
            // 校验token
            UserInfo userInfo = this.authService.verify(token);

            // 刷新时间，生成新的token
            token = this.authService.refreshToken(userInfo);
            if(StringUtils.isNotBlank(token)) {
                CookieUtils.setCookie(req, resp, cookieName, token,
                        null, null, true);
            }

            if (userInfo != null) {
                // 返回用户信息
                return ResponseEntity.ok(userInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
}
