package com.pinyougou.search.service;

import com.pinyougou.pojo.TbItem;

import java.util.List;
import java.util.Map;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.search.service
 * @since 1.0
 */
public interface ItemSearchService {

    /**
     *
     * @param searchMap  从页面传递过来的搜索条件的集合
     * @return  返回数据集合（包括分页的数据集合对象，总记录数 总页数 。...）
     */
    public Map  search(Map searchMap);

    /**
     * 更新数据到索引库中
     * @param items  就是数据
     */
    public void updateIndex(List<TbItem> items);

    /**
     * 根据goods_id域的值来删除
     * @param ids
     */
    public void deleteByQuery(Long[] ids);
}
