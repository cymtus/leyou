package com.leyou.search.client;

import com.leyou.item.api.SpecificationApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-06 10:44
 **/
@FeignClient(value = "item-service")
public interface SpecificationClient extends SpecificationApi {

}
