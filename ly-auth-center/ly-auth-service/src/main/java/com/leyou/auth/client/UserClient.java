package com.leyou.auth.client;

import com.leyou.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-14 12:27
 **/
@FeignClient("user-service")
public interface UserClient extends UserApi {
}
