package com.leyou.search.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-06 10:44
 **/
@FeignClient(value = "item-service")
public interface GoodsClient extends GoodsApi {

}
