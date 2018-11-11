package com.pinyougou.search.listener;

import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;
import java.util.List;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.search.listener
 * @since 1.0
 */
public class UpdateIndexListener implements MessageListener {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {

        if(message instanceof ObjectMessage){
            try {
                //接收消息 就是商品的ID 数组
                ObjectMessage objectMessage = (ObjectMessage)message;
                Long[] ids = (Long[]) objectMessage.getObject();//SPU的数组
                //从数据库中查询数据列表
                // 先注入商品的服务  调用商品的服务的方法获取商品的列表

                //更新索引库
                //1.查询SPU下的所有的SKU的列表数据
                List<TbItem> tbItems = goodsService.selectItemListByIds(ids);

                //2.将SKU的列表数据导入到索引库中
                //更新到索引库中
                itemSearchService.updateIndex(tbItems);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
