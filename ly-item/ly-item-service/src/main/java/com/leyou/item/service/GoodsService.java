package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.*;
import com.leyou.user.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-01 12:07
 **/
@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final Logger logger = LoggerFactory.getLogger(GoodsService.class);

    public PageResult<SpuBo> querySpuPage(
            Integer page, Integer rows, String sortBy, Boolean desc, Boolean saleable, String key) {

        // 分页
        PageHelper.startPage(page, rows);

        Example example = new Example(Spu.class);
        // 排序
        if (StringUtils.isNotBlank(sortBy)) {
            example.setOrderByClause(sortBy + (desc ? " DESC" : " ASC"));
        }
        // 查询
        if (StringUtils.isNotBlank(key)) {
            example.createCriteria().andLike("title", "%" + key + "%");
        }
        // 是否上下架
        if (saleable != null) {
            example.createCriteria().andEqualTo("saleable", saleable);
        }

        List<Spu> list = this.spuMapper.selectByExample(example);
        // 创建PageInfo
        PageInfo<Spu> info = new PageInfo<>(list);

        List<SpuBo> spus = new ArrayList<>();
        // 给每个spu查询分类名称和品牌名称
        for (Spu spu : info.getList()) {
            SpuBo spuBo = new SpuBo();
            // 普通属性
            BeanUtils.copyProperties(spu, spuBo);
            // 分类名称
            List<String> names = this.categoryService.queryNamesByIds(
                    Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setCname(StringUtils.join(names, "/"));
            // 品牌名称
            Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());
            spuBo.setBname(brand.getName());

            spus.add(spuBo);
        }
        // 返回分页结果
        return new PageResult<>(info.getTotal(), spus);
    }

    @Transactional
    public void saveGoods(SpuBo spu) {
        // 1、新增Spu
        // 1.1、完善数据
        spu.setId(null);
        spu.setSaleable(true);
        spu.setValid(true);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        // 1.2.保存
        this.spuMapper.insert(spu);

        // 2、新增SpuDetail
        spu.getSpuDetail().setSpuId(spu.getId());
        this.spuDetailMapper.insert(spu.getSpuDetail());

        // 3、新增SKU和Stock
        saveSkuAndStock(spu.getSkus(), spu.getId());

        try {
            // 发送消息
            this.amqpTemplate.convertAndSend("item.insert", spu.getId());
        } catch (Exception e) {
            logger.error("新增商品发送消息失败：spuId: {}", spu.getId(), e);
        }
    }

    private void saveSkuAndStock(List<Sku> skus, Long id) {
        //3、新增Sku
        for (Sku sku : skus) {
            // 判断是否有效
            if (!sku.getEnable()) {
                continue;
            }
            sku.setSpuId(id);
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insert(sku);

            // 4、新增Stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock().intValue());
            this.stockMapper.insertSelective(stock);
        }
    }

    public SpuDetail querySpuDetail(Long id) {
        return this.spuDetailMapper.selectByPrimaryKey(id);
    }


    public List<Sku> querySkuList(Long spuId) {
        // 查询sku
        Sku record = new Sku();
        record.setSpuId(spuId);
        List<Sku> skus = this.skuMapper.select(record);
        for (Sku sku : skus) {
            // 同时查询出库存
            sku.setStock(this.stockMapper.selectByPrimaryKey(sku.getId()).getStock());
        }
        return skus;
    }

    @Transactional
    public void updateGoods(SpuBo spu) {
        // 查询以前sku
        List<Sku> skus = this.querySkuList(spu.getId());
        // 如果以前存在，则删除
        if (!CollectionUtils.isEmpty(skus)) {
            List<Long> ids = skus.stream().map(s -> s.getId()).collect(Collectors.toList());
            // 删除以前库存
            Example example = new Example(Stock.class);
            example.createCriteria().andIn("skuId", ids);
            this.stockMapper.deleteByExample(example);

            // 删除以前的sku
            Sku record = new Sku();
            record.setSpuId(spu.getId());
            this.skuMapper.delete(record);

        }
        // 新增sku和库存
        saveSkuAndStock(spu.getSkus(), spu.getId());

        // 更新spu
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);
        spu.setValid(null);
        spu.setSaleable(null);
        this.spuMapper.updateByPrimaryKeySelective(spu);

        // 更新spu详情
        this.spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());

        try {
            // 发送消息
            this.amqpTemplate.convertAndSend("item.update", spu.getId());
        } catch (Exception e) {
            logger.error("修改商品发送消息失败：spuId: {}", spu.getId(), e);
        }
    }

    public Spu querySpuById(Long id) {
        return this.spuMapper.selectByPrimaryKey(id);
    }
}
