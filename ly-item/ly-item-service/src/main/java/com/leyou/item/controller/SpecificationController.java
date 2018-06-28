package com.leyou.item.controller;

import com.leyou.item.service.SpecificationService;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.Around;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-01 10:40
 **/
@RequestMapping("spec")
@RestController
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    /**
     * 根据id查询商品规格
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<String> querySpecById(@PathVariable("id") Long id){
        String spec = this.specificationService.querySpecById(id);
        if(StringUtils.isBlank(spec)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(spec);
    }
}
