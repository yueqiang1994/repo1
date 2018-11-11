package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.common.pojo.SysConstants;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import javax.sound.midi.Soundbank;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.search.service.impl
 * @since 1.0
 */
@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Override
    public Map search(Map searchMap) {
        Map resultMap = new HashMap();
        //主搜索
        Map map = searchList(searchMap);
        resultMap.putAll(map);
        //分组搜索
        List<String> categoryList = findCategoryList(searchMap);//手机    平板电视
        resultMap.put("categoryList",categoryList);
        //查询品牌列表和规格的列表 默认 查询第一个分类下的所有的品牌列表和规格的列表
        String category = (String) searchMap.get("category");
        Map specBrandMap= new HashMap() ;
        if(StringUtils.isNotBlank(category)) {
            specBrandMap = findSpecListAndBrandList(category);
        }else{
            specBrandMap = findSpecListAndBrandList(categoryList.get(0));
        }
        resultMap.putAll(specBrandMap);
        return resultMap;
    }

    @Override
    public void updateIndex(List<TbItem> items) {
        solrTemplate.saveBeans(items);
        solrTemplate.commit();
    }

    @Override
    public void deleteByQuery(Long[] ids) {
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid");
        criteria.in(ids);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    @Autowired
    private RedisTemplate redisTemplate;

    private Map findSpecListAndBrandList(String categoryName){
        Map sepcBrandMap = new HashMap();
        Long typeId = (Long) redisTemplate.boundHashOps(SysConstants.SEARCH_REDIS_ITEM_CAT_KEY).get(categoryName);

        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps(SysConstants.SEARCH_REDIS_TYPE_TEMPLATE_BRAND_lIST_KEY).get(typeId);
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps(SysConstants.SEARCH_REDIS_TYPE_TEMPLATE_SPEC_lIST_KEY).get(typeId);

        sepcBrandMap.put("brandList",brandList);
        sepcBrandMap.put("specList",specList);
        return sepcBrandMap;
    }

    private List<String> findCategoryList(Map searchMap){
        List<String> categoryList = new ArrayList<>();
        Query query = new SimpleQuery();
        //2.获取从页面传递过来的参数的值，并设置条件
        String keywords = (String) searchMap.get("keywords");//三星
        keywords=keywords.replaceAll(" ","");
        Criteria criteria = new Criteria("item_keywords");//item_keywords:三星
        criteria.is(keywords);
        query.addCriteria(criteria);

        //3.分组查询
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");//根据什么来分组（分组的域是什么）  类似  select * from tbitem where group by category 中的 group by
        query.setGroupOptions(groupOptions);

        //4.查询
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);// select * from tbitem
        GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        List<GroupEntry<TbItem>> content = groupEntries.getContent();//只有分组的数据
        for (GroupEntry<TbItem> tbItemGroupEntry : content) {
            String groupValue = tbItemGroupEntry.getGroupValue();//手机     平板电视
            categoryList.add(groupValue);
        }

        return categoryList;
    }

    private Map searchList(Map searchMap){
        Map map = new HashMap();
        //1.创建一个查询的对象
        HighlightQuery query = new SimpleHighlightQuery();
        //2.获取从页面传递过来的参数的值，并设置条件
        String keywords = (String) searchMap.get("keywords");//三星
        keywords=keywords.replaceAll(" ","");
        Criteria criteria = new Criteria("item_keywords");//item_keywords:三星
        criteria.is(keywords);
        query.addCriteria(criteria);
        //3.高亮的设置 开启高亮  设置高亮显示的域 以及 前缀和后缀

        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");//添加高亮显示的域
        highlightOptions.setSimplePrefix("<em style=\"color:red\">");
        highlightOptions.setSimplePostfix("</em>");
        query.setHighlightOptions(highlightOptions);


        //4.过滤查询    品牌
        String brand = (String) searchMap.get("brand");
        if(StringUtils.isNotBlank(brand)){
            FilterQuery filterquery = new SimpleFilterQuery();//item_brand:三星
            Criteria criteria1 = new Criteria("item_brand");
            criteria1.is(brand);
            filterquery.addCriteria(criteria1);
            query.addFilterQuery(filterquery);
        }


        //5.过滤查询    分类
        String category = (String) searchMap.get("category");
        if(StringUtils.isNotBlank(category)){
            FilterQuery filterquery = new SimpleFilterQuery();//item_brand:三星
            Criteria criteria1 = new Criteria("item_category");
            criteria1.is(category);
            filterquery.addCriteria(criteria1);
            query.addFilterQuery(filterquery);
        }
        //6.过滤查询  规格
        Map<String,String> spec = (Map<String, String>) searchMap.get("spec");
        if(spec!=null){
            for (String key : spec.keySet()) {
                FilterQuery filterquery = new SimpleFilterQuery();//
                Criteria criteria1 = new Criteria("item_spec_"+key);//item_spec_网络
                criteria1.is(spec.get(key));//移动4G ====item_spec_网络:移动4G
                filterquery.addCriteria(criteria1);
                query.addFilterQuery(filterquery);
            }
        }

        //7.价格区间的过滤
        String price = (String) searchMap.get("price");//0-500
        if(StringUtils.isNotBlank(price)){
            //切割
            String[] split = price.split("-");

            FilterQuery filterquery = new SimpleFilterQuery();//
            Criteria criteria1 = new Criteria("item_price");//item_price
            //item_price:[30000 TO *]
            if(split[1].equals("*")){
                criteria1.greaterThanEqual(split[0]);
            }else {
                criteria1.between(split[0], split[1], true, true);
            }
            filterquery.addCriteria(criteria1);
            query.addFilterQuery(filterquery);
        }


        //8.按照价格排序
        String sortType = (String) searchMap.get("sortType");//ASC / DESC
        String sortField = (String) searchMap.get("sortField");//price  / pinjia  /xinpin  /xiaoliang

        if(StringUtils.isNotBlank(sortType) && StringUtils.isNotBlank(sortField)) {
            Sort sort=null;
            if(sortType.equals("ASC")) {
                sort = new Sort(Sort.Direction.ASC, "item_"+sortField);//对item_price的域进行升序
                query.addSort(sort);
            }else{
                sort = new Sort(Sort.Direction.DESC, "item_"+sortField);//对item_price的域进行升序
                query.addSort(sort);
            }
        }


        //9.分页的设置
        Integer pageNo= (Integer) searchMap.get("pageNo");
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if(pageNo==null){
            pageNo=1;
        }
        if(pageSize==null){
            pageSize=40;
        }
        query.setOffset((pageNo-1)*pageSize);//第一页  (page-1)*rows
        query.setRows(pageSize);//每页显示40行 //rows





        //执行查询
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);

        //获取高亮数据

        List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();
        if(highlighted!=null && highlighted.size()>0) {
            for (HighlightEntry<TbItem> tbItemHighlightEntry : highlighted) {
                if(tbItemHighlightEntry!=null) {
                    TbItem entity = tbItemHighlightEntry.getEntity();//该对象就是某一个文档对应的pojo对象
                    List<HighlightEntry.Highlight> highlights = tbItemHighlightEntry.getHighlights();
                    if(highlights != null &&
                            highlights.size()>0 &&
                            highlights.get(0)!=null &&
                            highlights.get(0).getSnipplets()!=null &&
                            highlights.get(0).getSnipplets().size()>0
                            ) {
                        entity.setTitle(highlights.get(0).getSnipplets().get(0));
                    }
                }
            }
        }


        //从结果中获取总页数 总记录数 列表
        List<TbItem> content = page.getContent();
        //设置结果
        map.put("rows",content);//当前页的记录集合
        map.put("total",page.getTotalElements());//总记录数
        map.put("totalPages",page.getTotalPages());//总页数
        //返回
        return map;
    }
}
