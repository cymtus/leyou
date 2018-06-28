package com.leyou.page.client;

import com.leyou.item.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-06 10:44
 **/
@FeignClient(value = "item-service")
public interface BrandsClient extends BrandApi {

}
