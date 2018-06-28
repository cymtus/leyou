package com.leyou.user.service;

import com.leyou.user.pojo.User;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.utils.CodecUtils;
import com.leyou.utils.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-12 11:14
 **/
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    static final String KEY_PREFIX = "user:code:phone:";

    public Boolean checkData(String data, Integer type) {
        User user = new User();

        switch (type) {
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                return null;
        }

        int count = this.userMapper.selectCount(user);
        return count == 0;
    }

    public void sendCode(String phone) {
        // 1、生成验证码
        String code = NumberUtils.generateCode(6);
        // 2、发送短信
        Map<String, String> msg = new HashMap<>();
        msg.put("phone", phone);
        msg.put("code", code);

        String exchange = "ly.sms.exchange";
        String routingKey = "sms.verify.code";
        this.amqpTemplate.convertAndSend(exchange, routingKey, msg);
        // 3、保存验证码
        this.redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 5, TimeUnit.MINUTES);
    }

    public Boolean register(User user, String code) {
        // 1、校验验证码
        String cacheCode = this.redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        if (!StringUtils.equals(code, cacheCode)) {
            return null;
        }

        // 2、对密码进行加密
        // 2.1、生成盐
        String salt = CodecUtils.generateSalt();
        // 2.2、密码加密
        String password = CodecUtils.md5Hex(user.getPassword(), salt);
        // 2.3、保存密文信息
        user.setPassword(password);
        user.setSalt(salt);

        // 3、补充用户信息
        user.setCreated(new Date());

        // 4、新增用户
        int count = this.userMapper.insert(user);

        return count == 1;
    }

    public User queryUser(String username, String password) {
        // 校验用户名
        User t = new User();
        t.setUsername(username);
        User user = this.userMapper.selectOne(t);
        if (user == null) {
            return null;
        }
        // 校验密码
        String pw = CodecUtils.md5Hex(password, user.getSalt());
        if (!StringUtils.equals(user.getPassword(), pw)) {
            return null;
        }
        return user;
    }
}
