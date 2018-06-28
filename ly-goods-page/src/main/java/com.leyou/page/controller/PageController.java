package com.leyou.page.controller;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-09 10:44
 **/
@Controller
public class PageController {

    @Autowired
    private PageService pageService;

    @GetMapping("item/{id}.html")
    public String hello(Model model, @PathVariable("id") Long id) {
        // 查询模型数据
        Map<String,Object> map = this.pageService.loadModel(id);
        model.addAllAttributes(map);
        model.addAttribute("categories", map.get("categories"));

        // 生成静态页面
        this.pageService.syncCreateHtml(id);
        return "item";
    }
}
