package com.leyou.item.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-08 11:51
 **/
@RequestMapping("spec")
public interface SpecificationApi {

    @GetMapping("{id}")
    ResponseEntity<String> querySpecById(@PathVariable("id") Long id);
}
