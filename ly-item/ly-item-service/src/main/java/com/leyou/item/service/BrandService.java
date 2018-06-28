package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.user.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author: HuYi.Zhang
 * @create: 2018-05-28 10:00
 **/
@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryBrandPage(
            Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        // 分页
        PageHelper.startPage(page, rows);

        Example example = new Example(Brand.class);
        // 排序
        if (StringUtils.isNotBlank(sortBy)) {
            example.setOrderByClause(sortBy + (desc ? " DESC" : " ASC"));
        }
        // 查询
        if(StringUtils.isNotBlank(key)) {
            example.createCriteria().orLike("name", key)
                    .orEqualTo("letter", key.toUpperCase());
        }
        List<Brand> list = this.brandMapper.selectByExample(example);
        // 创建PageInfo
        PageInfo<Brand> info = new PageInfo<>(list);
        // 返回分页结果
        return new PageResult<>(info.getTotal(), info.getList());
    }

    @Transactional
    public void save(Brand brand, List<Long> ids) {
        // 新增品牌
        this.brandMapper.insert(brand);
        // 新增品牌和分类中间表
        for (Long cid : ids) {
            this.brandMapper.insertCategoryBrand(cid, brand.getId());
        }
    }

    public List<Brand> queryBrandByCategory(Long id) {
        return this.brandMapper.queryByCategoryId(id);
    }

    public List<Brand> queryBrandByIds(List<Long> ids) {
        return this.brandMapper.selectByIdList(ids);
    }
}
