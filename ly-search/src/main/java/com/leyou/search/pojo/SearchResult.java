package com.leyou.search.pojo;

import com.leyou.common.pojo.PageResult;
import com.leyou.user.pojo.Brand;
import com.leyou.user.pojo.Category;

import java.util.List;
import java.util.Map;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-08 10:14
 **/
public class SearchResult<T> extends PageResult<T> {

    private List<Category> categories;

    private List<Brand> brands;

    private List<Map<String, Object>> specs;

    public SearchResult(List<Category> categories, List<Brand> brands) {
        this.categories = categories;
        this.brands = brands;
    }

    public SearchResult(Long total, List<T> items, List<Category> categories, List<Brand> brands) {
        super(total, items);
        this.categories = categories;
        this.brands = brands;
    }

    public SearchResult(Long total, Long totalPage, List<T> items, List<Category> categories, List<Brand> brands) {
        super(total, totalPage, items);
        this.categories = categories;
        this.brands = brands;
    }

    public SearchResult(Long total, Long totalPage, List<T> items,
                        List<Category> categories, List<Brand> brands,
                        List<Map<String, Object>> specs) {
        super(total, totalPage, items);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Brand> getBrands() {
        return brands;
    }

    public void setBrands(List<Brand> brands) {
        this.brands = brands;
    }

    public List<Map<String, Object>> getSpecs() {
        return specs;
    }

    public void setSpecs(List<Map<String, Object>> specs) {
        this.specs = specs;
    }
}
