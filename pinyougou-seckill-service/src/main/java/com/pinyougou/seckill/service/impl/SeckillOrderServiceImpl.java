package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.common.pojo.SysConstants;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.pojo.RecordInfo;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.seckill.thread.CreateOrderHandlerThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Date;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.seckill.service.impl
 * @since 1.0
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private CreateOrderHandlerThread createOrderHandlerThread;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public void addOrder(Long id, String userId) {
        //1.根据秒杀商品的ID 获取商品的全部信息（从redis中获取）
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(id);

        //2.判断商品是否已经售罄（没有库存了）

//        if(seckillGoods==null || seckillGoods.getStockCount()<=0){
//            throw new RuntimeException("商品已经售罄");
//        }
        Object o = redisTemplate.boundListOps(SysConstants.SEC_KILL_GOODS_PREFIX + id).rightPop();

        //判断是否已经售罄 通过队列的方式
        if(o==null){
            throw new RuntimeException("商品已经售罄");
        }


        //排除  用户已经有未支付的订单的用户

        Boolean flag = redisTemplate.boundHashOps("seckillOrder").hasKey(userId);
        if(flag){
            throw new RuntimeException("您先支付");
        }


        //排除抢购上限的用户   我们定义的规则 允许  用户的量不能超过库存
        Long size = redisTemplate.boundListOps(SysConstants.SEC_KILL_LIMIT_PREFIX + id).size();
        if(size>seckillGoods.getStockCount()){
            throw new RuntimeException("商品抢购人数已经达到上限");
        }

        //排除 用户正在排队的情况
        Boolean aBoolean = redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).hasKey(userId);
        if(aBoolean){
            throw new RuntimeException("您正在排队中，别点了");
        }
        //用户排队 表示用户进入排队中（要创建订单的）队列中的元素数据包括两个：userId id
        RecordInfo info = new RecordInfo();
        info.setUserId(userId);
        info.setId(id);
        redisTemplate.boundListOps(SysConstants.SEC_KILL_USER_ORDER_LIST).leftPush(info);


        //设置某一个用户在排队 的标识
        redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).put(userId,id);




        //限制商品的抢购上限 压入队列（用户来一个就排队一个）
        redisTemplate.boundListOps(SysConstants.SEC_KILL_LIMIT_PREFIX+id).leftPush(id);


        //多线程调用创建订单（从排队队列中获取）
//        new Thread(createOrderHandlerThread).start();
        threadPoolTaskExecutor.execute(createOrderHandlerThread);

    }

    @Override
    public TbSeckillOrder getFromRedis(String userId) {
        return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
    }

    @Override
    public boolean isFlag(String userId) {
        return redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).hasKey(userId);
    }


}
