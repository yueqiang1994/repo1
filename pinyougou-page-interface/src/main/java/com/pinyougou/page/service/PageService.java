package com.pinyougou.page.service;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.page.service
 * @since 1.0
 */
public interface PageService {
    /**
     * 根据SPU商品的ID 生成静态页面
     * @param id
     */
    public void genHtmlByGoodsId(Long id);
}
