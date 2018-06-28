package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.user.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-14 12:20
 **/
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties jwtProperties;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);


    public String login(String username, String password) {
        try {
            // 查询用户信息
            ResponseEntity<User> resp = this.userClient.queryUser(username, password);
            if (!resp.hasBody()) {
                return null;
            }
            // 查询成功，获取用户
            User user = resp.getBody();

            // 生成token
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            String token = JwtUtils.generateToken(userInfo,
                    jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            return token;
        } catch (Exception e){
            logger.error("生成token失败，用户名：{}", username, e);
        }
        return null;
    }

    public UserInfo verify(String token) throws Exception {

        return JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
    }

    public String refreshToken(UserInfo userInfo) {
        try {
            return JwtUtils.generateToken(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
        } catch (Exception e) {
            logger.error("生成token失败，用户名：{}", userInfo.getUsername(), e);
            return null;
        }
    }
}
