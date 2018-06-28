package com.leyou.page.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-09 12:11
 **/
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {
}
