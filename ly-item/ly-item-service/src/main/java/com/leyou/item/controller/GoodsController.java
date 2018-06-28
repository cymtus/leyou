package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.user.pojo.Sku;
import com.leyou.user.pojo.Spu;
import com.leyou.user.pojo.SpuBo;
import com.leyou.user.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-01 12:06
 **/
@RestController
@RequestMapping
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "desc", defaultValue = "false") Boolean desc,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "key", required = false) String key) {
        PageResult<SpuBo> result = this.goodsService.querySpuPage(page, rows, sortBy, desc, saleable, key);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 新增商品
     * @param spu
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spu){
        this.goodsService.saveGoods(spu);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 修改商品
     * @param spu
     * @return
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spu){
        this.goodsService.updateGoods(spu);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 根据id查询商品详情
     * @param id
     * @return
     */
    @GetMapping("spu/detail/{id}")
    public ResponseEntity<SpuDetail> querySpuDetail(@PathVariable("id") Long id){
        SpuDetail spuDetail = this.goodsService.querySpuDetail(id);
        return ResponseEntity.ok(spuDetail);
    }

    /**
     * 根据id查询sku列表
     * @param id
     * @return
     */
    @GetMapping("/sku/list")
    public ResponseEntity<List<Sku>> querySkuList(@RequestParam("id") Long id){
        List<Sku> skus = this.goodsService.querySkuList(id);
        return ResponseEntity.ok(skus);
    }

    @GetMapping("/spu/{id}")
    ResponseEntity<Spu> querySpuById(@PathVariable("id") Long id){
        Spu spu = this.goodsService.querySpuById(id);
        if(spu == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(spu);
    }
}
