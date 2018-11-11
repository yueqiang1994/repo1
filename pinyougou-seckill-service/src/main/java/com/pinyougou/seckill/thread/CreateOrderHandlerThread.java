package com.pinyougou.seckill.thread;

import com.pinyougou.common.pojo.SysConstants;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.pojo.RecordInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.seckill.thread
 * @since 1.0
 */
@Component
@Scope("prototype")
public class CreateOrderHandlerThread implements Runnable {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;


    @Override
    public void run() {
        //先从排队队列中获取 秒杀商品的信息以及用户的信息===》队列中的元素数据包括两个：userId id
//        保证公平  原来是从左边压队列元素 取就要右边弹出元素
        RecordInfo info = (RecordInfo) redisTemplate.boundListOps(SysConstants.SEC_KILL_USER_ORDER_LIST).rightPop();

        if(info!=null) {
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(info.getId());


            //创建订单成功，就弹出一个排队人
            redisTemplate.boundListOps(SysConstants.SEC_KILL_LIMIT_PREFIX+info.getId()).rightPop();



            //3.减库存,存储到redis中
            seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
            redisTemplate.boundHashOps("seckillGoods").put(info.getId(), seckillGoods);


            if (seckillGoods.getStockCount() <= 0) {
                //更新数据库的数据，并且删除redis中的商品
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                redisTemplate.boundHashOps("seckillGoods").delete(info.getId());
            }
            //4.下预订单在redis中
            TbSeckillOrder order = new TbSeckillOrder();
            order.setId(new IdWorker(1, 1).nextId());
            order.setSeckillId(info.getId());
            order.setMoney(seckillGoods.getCostPrice());//打折价（秒杀价） 只能买一个
            order.setSellerId(seckillGoods.getSellerId());
            order.setCreateTime(new Date());
            order.setStatus("0");//一定是未支付的状态
            //存储订单到redis中
            redisTemplate.boundHashOps("seckillOrder").put(info.getUserId(), order);


            //下单成功之后删除排队标识
            redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).delete(info.getUserId());
        }
    }
}
