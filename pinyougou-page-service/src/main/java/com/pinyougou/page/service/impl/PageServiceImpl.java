package com.pinyougou.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.PageService;
import com.pinyougou.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.page.service.impl
 * @since 1.0
 */
@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private TbGoodsMapper tbGoodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private FreeMarkerConfigurer configurer;
    @Override
    public void genHtmlByGoodsId(Long id) {

        //1.根据商品的ID 获取商品的数据 SPU  查询商品的描述信息
        TbGoods goods = tbGoodsMapper.selectByPrimaryKey(id);
        TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);



        //2.调用freemarker代码实现页面生成  数据集  + 模板 =html  和spring整合

        genhtmlFreemarker(goods,goodsDesc,"item.ftl");



    }

    private void genhtmlFreemarker(TbGoods goods, TbGoodsDesc goodsDesc, String template) {
        FileWriter writer=null;
        try {
            //数据集  +模板= html
            //1.创建一个confiruration
            //2.设置模板所在的目录 以及模板的字符编码
            Configuration configuration = configurer.getConfiguration();
            //3.加载模板的对象
            Template templateObject = configuration.getTemplate(template);
            //4.数据集 就是传递过来的godos 和goodesc

            Map model = new HashMap();

            model.put("goods",goods);


            model.put("goodsDesc",goodsDesc);

            //根据分类的ID 查询分类的对象
            TbItemCat tbItemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id());
            TbItemCat tbItemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id());
            TbItemCat tbItemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id());
            model.put("tbItemCat1",tbItemCat1.getName());
            model.put("tbItemCat2",tbItemCat2.getName());
            model.put("tbItemCat3",tbItemCat3.getName());

            //根据SPU的ID 查所有的SPU对应的SKU列表 并且按照 is_default排序 desc
            //select * from tb_item where goods_id = 1 and status=1 order by is_default desc
            TbItemExample exmaple = new TbItemExample();
            TbItemExample.Criteria criteria = exmaple.createCriteria();
            criteria.andGoodsIdEqualTo(goods.getId());
            criteria.andStatusEqualTo("1");

            exmaple.setOrderByClause("is_default desc");//order by is_default desc
            List<TbItem> tbItems = itemMapper.selectByExample(exmaple);
            model.put("skuList",tbItems);

            //5.创建流对象输出到某一个目录和html 全路径
            writer = new FileWriter(new File("G:\\freemarker\\"+goods.getId()+".html"));

            //6.输出
            templateObject.process(model,writer);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(writer!=null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
