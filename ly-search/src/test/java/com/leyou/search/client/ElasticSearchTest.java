package com.leyou.search.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.LySearchService;
import com.leyou.common.pojo.PageResult;
import com.leyou.user.pojo.Sku;
import com.leyou.user.pojo.SpuBo;
import com.leyou.user.pojo.SpuDetail;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LySearchService.class)
public class ElasticSearchTest {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private ElasticsearchTemplate template;

    @Test
    public void testCreate(){
        this.template.createIndex(Goods.class);
        this.template.putMapping(Goods.class);
    }

    @Test
    public void testAddGoods(){
        this.goodsRepository.deleteAll();

        int page = 1;
        int rows = 100;
        int size = 0;
        do {
            // 查询SPU
            ResponseEntity<PageResult<SpuBo>> spuResp =
                    this.goodsClient.querySpuByPage(page, rows, null, null, true, null);
            if(!spuResp.hasBody()){
                break;
            }
            List<SpuBo> spus = spuResp.getBody().getItems();
            // 创建Goods的集合
            List<Goods> goodsList = new ArrayList<>();
            for (SpuBo spu : spus) {
                // 查询spuDetail
                ResponseEntity<SpuDetail> detailResp = this.goodsClient.querySpuDetail(spu.getId());
                // 查询sku
                ResponseEntity<List<Sku>> skuResp = this.goodsClient.querySkuList(spu.getId());
                // 查询商品分类名称
                ResponseEntity<List<String>> nameResp =
                        this.categoryClient.queryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

                // 判断是否存在
                if(!detailResp.hasBody() || !skuResp.hasBody() || !nameResp.hasBody()){
                    break;
                }
                List<String> names = nameResp.getBody();
                SpuDetail spuDetail = detailResp.getBody();
                List<Sku> skus = skuResp.getBody();

                // 处理sku
                List<Map<String,Object>> skuList = new ArrayList<>();
                Set<Long> priceList = new HashSet<>();
                for (Sku sku : skus) {
                    priceList.add(sku.getPrice());
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", sku.getId());
                    map.put("price", sku.getPrice());
                    map.put("image", StringUtils.isBlank(sku.getImages()) ? "" : sku.getImages().split(",")[0]);
                    map.put("title", sku.getTitle());
                    skuList.add(map);
                }

                // 处理商品规格
                String json = spuDetail.getSpecifications();
                List<Map<String,Object>> list = JsonUtils.nativeRead(json, new TypeReference<List<Map<String, Object>>>() {
                });
                Map<String, Object> specs = new HashMap<>();

                // 遍历
                for (Map<String, Object> map : list) {
                    List<Map<String,Object>> params = (List<Map<String, Object>>) map.get("params");
                    for (Map<String, Object> param : params) {
                        Object searchable = param.get("searchable");
                        if(Boolean.valueOf(searchable.toString())){
                            if(param.get("v") != null){
                                specs.put(param.get("k").toString(), param.get("v"));
                            } else if(param.get("options") != null){
                                specs.put(param.get("k").toString(), param.get("options"));
                            }
                        }
                    }
                }
                Goods goods = new Goods();
                goods.setSpecs(specs);
                goods.setSkus(JsonUtils.serialize(skuList));
                goods.setPrice(new ArrayList<>(priceList));
                goods.setAll(spu.getTitle() + StringUtils.join(names, " "));
                goods.setSubTitle(spu.getSubTitle());
                goods.setId(spu.getId());
                goods.setCreateTime(spu.getCreateTime());
                goods.setCid1(spu.getCid1());
                goods.setCid2(spu.getCid2());
                goods.setCid3(spu.getCid3());
                goods.setBrandId(spu.getBrandId());
                goodsList.add(goods);
            }

            // 把goods放到索引库
            this.goodsRepository.saveAll(goodsList);
            // TODO 给size赋值
            size = spus.size();
            // 翻页
            page++;
        } while (size == 100);
    }

}