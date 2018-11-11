package com.pinyougou.order.service;

import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbPayLog;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.order.service
 * @since 1.0
 */
public interface OrderService {

    /**
     * 创建订单
     * @param order
     */
    public void add(TbOrder order);

    /**
     * 根据用户的ID 查询
     * @param userId
     * @return
     */
    public TbPayLog getTbPayLogFromRedis(String userId);

    /**
     * 更新支付日志记录（根据主键来更新）
     * 更新支付日志记录中的（transaction_id）
     *
     */
    public void updateTbPayLogAndTbOrder(String out_trade_no,String transaction_id);
}
