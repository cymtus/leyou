package com.leyou.user.api;

import com.leyou.user.pojo.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-14 12:24
 **/
public interface UserApi {

    @GetMapping("query")
    ResponseEntity<User> queryUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password);
}
