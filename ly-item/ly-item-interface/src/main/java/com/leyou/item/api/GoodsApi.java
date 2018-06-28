package com.leyou.item.api;

import com.leyou.common.pojo.PageResult;
import com.leyou.user.pojo.Sku;
import com.leyou.user.pojo.Spu;
import com.leyou.user.pojo.SpuBo;
import com.leyou.user.pojo.SpuDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-06 10:48
 **/
@RequestMapping
public interface GoodsApi {

    @GetMapping("spu/page")
    ResponseEntity<PageResult<SpuBo>> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "desc", defaultValue = "false") Boolean desc,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "key", required = false) String key);

    @GetMapping("spu/detail/{id}")
    ResponseEntity<SpuDetail> querySpuDetail(@PathVariable("id") Long id);

    @GetMapping("/sku/list")
    ResponseEntity<List<Sku>> querySkuList(@RequestParam("id") Long id);

    @GetMapping("/spu/{id}")
    ResponseEntity<Spu> querySpuById(@PathVariable("id") Long id);
}
