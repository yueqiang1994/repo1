package com.pinyougou.page.listener;

import com.pinyougou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.page.listener
 * @since 1.0
 */
public class GenHtmlListener implements MessageListener {

    @Autowired
    private PageService pageService;
    @Override
    public void onMessage(Message message) {
        if (message instanceof ObjectMessage) {
            try {
                //接收消息
                ObjectMessage message1 =(ObjectMessage)message;

                Long[] ids = (Long[]) message1.getObject();
                //生成静态化页面
                for (Long id : ids) {
                    pageService.genHtmlByGoodsId(id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
