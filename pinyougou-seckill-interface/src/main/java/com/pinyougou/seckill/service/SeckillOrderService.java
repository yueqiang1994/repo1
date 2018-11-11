package com.pinyougou.seckill.service;

import com.pinyougou.pojo.TbSeckillOrder;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.seckill.service
 * @since 1.0
 */
public interface SeckillOrderService {

    /**
     *
     * @param id  下单的商品的id
     * @param  userId  登录的用户
     */
    public void addOrder(Long id,String userId);

    /**
     * 获取登录用户的支付订单
     * @param userId
     * @return
     */
    public TbSeckillOrder getFromRedis(String userId);

    /**
     * 判断用户是否在排队
     * @param userId
     * @return
     */
    public boolean isFlag(String userId);
}
