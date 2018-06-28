package com.leyou.page.service;

import com.leyou.page.client.BrandsClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.user.pojo.*;
import com.leyou.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-09 12:10
 **/
@Service
public class PageService {

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private BrandsClient brandsClient;

    @Value("${ly.file.path}")
    private String destPath;

    private static final Logger logger = LoggerFactory.getLogger(PageService.class);

    public Map<String, Object> loadModel(Long id) {
        // 模型数据
        Map<String, Object> modelMap = new HashMap<>();

        // 1、查询spu
        ResponseEntity<Spu> spuResp = this.goodsClient.querySpuById(id);
        // 2、查询spuDetail
        ResponseEntity<SpuDetail> detailResp = this.goodsClient.querySpuDetail(id);
        // 3、查询sku
        ResponseEntity<List<Sku>> skusResp = this.goodsClient.querySkuList(id);
        if (!spuResp.hasBody() || !detailResp.hasBody() || !skusResp.hasBody()) {
            return null;
        }
        Spu spu = spuResp.getBody();
        SpuDetail detail = detailResp.getBody();
        List<Sku> skus = skusResp.getBody();
        modelMap.put("spu", spu);
        modelMap.put("detail", detail);
        modelMap.put("skus", skus);

        // 4、商品分类
        List<Category> categories = getCategories(spu);
        if (categories != null) {
            modelMap.put("categories", categories);
        }

        // 5、准备品牌数据
        ResponseEntity<List<Brand>> brandResp = this.brandsClient.queryBrandByIds(
                Arrays.asList(spu.getBrandId()));
        if (brandResp.hasBody()) {
            modelMap.put("brand", brandResp.getBody().get(0));
        }

        return modelMap;
    }

    private List<Category> getCategories(Spu spu) {
        try {
            ResponseEntity<List<String>> categoryResp = this.categoryClient.queryNameByIds(
                    Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            if (categoryResp.hasBody()) {
                List<String> names = categoryResp.getBody();

                Category c1 = new Category();
                c1.setName(names.get(0));
                c1.setId(spu.getCid1());

                Category c2 = new Category();
                c2.setName(names.get(1));
                c2.setId(spu.getCid2());

                Category c3 = new Category();
                c3.setName(names.get(2));
                c3.setId(spu.getCid3());

                return Arrays.asList(c1, c2, c3);
            }

        } catch (Exception e) {
            logger.error("查询商品分类出错，spuId：{}", spu.getId(), e);
        }
        return null;
    }

    /**
     * 创建html页面
     *
     * @param id
     * @throws Exception
     */
    public void createHtml(Long id) throws Exception {
        // 创建上下文，
        Context context = new Context();
        // 把数据加入上下文
        context.setVariables(this.loadModel(id));

        // 创建输出流，关联到一个临时文件
        File temp = new File(id + ".html");
        // 目标页面文件
        File dest = createPath(id);
        // 备份原页面文件
        File bak = new File(id + "_bak.html");
        try (PrintWriter writer = new PrintWriter(temp, "UTF-8")) {
            // 利用thymeleaf模板引擎生成 静态页面
            templateEngine.process("item", context, writer);

            if (dest.exists()) {
                // 如果目标文件已经存在，先备份
                dest.renameTo(bak);
            }
            // 将新页面覆盖旧页面
            FileCopyUtils.copy(temp, dest);
            // 成功后将备份页面删除
            bak.delete();
        } catch (IOException e) {
            // 失败后，将备份页面恢复
            bak.renameTo(dest);
            // 重新抛出异常，声明页面生成失败
            throw new Exception(e);
        } finally {
            // 删除临时页面
            if (temp.exists()) {
                temp.delete();
            }
        }
    }

    private File createPath(Long id) {
        if (id == null) {
            return null;
        }
        File dest = new File(this.destPath);
        if (!dest.exists()) {
            dest.mkdirs();
        }
        return new File(dest, id + ".html");
    }

    /**
     * 判断某个商品的页面是否存在
     *
     * @param id
     * @return
     */
    public boolean exists(Long id) {
        return this.createPath(id).exists();
    }

    /**
     * 异步创建html页面
     *
     * @param id
     */
    public void syncCreateHtml(Long id) {
        ThreadUtils.execute(() -> {
            try {
                createHtml(id);
            } catch (Exception e) {
                // 重新抛出异常，声明页面生成失败
                logger.error("创建静态页面失败：id:{}", id, e);
            }
        });
    }

    public void deleteHtml(Long id) {
        this.createPath(id).deleteOnExit();
    }
}
