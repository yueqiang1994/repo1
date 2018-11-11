package com.pinyougou.util;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.util
 * @since 1.0
 */
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    public void importDataToIndex(){
        //1.从数据库查询所有的数据
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//有效的商品
        List<TbItem> tbItems = itemMapper.selectByExample(example);

        for (TbItem tbItem : tbItems) {
            String spec = tbItem.getSpec();//{"机身内存":"16G","网络":"联通3G"}
            Map<String,String> map = JSON.parseObject(spec, Map.class);
           // Map<String,String> map = new HashMap<>();//{"网络制式":"移动3G"，"机身内存":"16G"}
            tbItem.setSpecMap(map);
        }

        //2.调用spring data solr中的solrTemplate方法
        solrTemplate.saveBeans(tbItems);
        solrTemplate.commit();
    }

    public static void main(String[] args){
        //1.初始化spring容器
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-*.xml");
        SolrUtil bean = context.getBean(SolrUtil.class);
        bean.importDataToIndex();
    }
}
