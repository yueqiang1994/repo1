package com.pinyougou.seckill.task;

import com.pinyougou.common.pojo.SysConstants;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 任务类 用于执行某一些定时任务的
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyou.seckill.task
 * @since 1.0
 */
@Component
public class TaskSeckillGoodsRedisList {

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 定时每隔30秒就执行一次（查询数据库的秒杀商品的数据到redis中）
     */

    @Scheduled(cron = "0/5 * * * * ? ")
    public void pushGoods(){
        //1.从数据中查询秒杀商品的数据列表

        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");
        criteria.andStockCountGreaterThan(0);
        Date date = new Date();
        //开始时间  《 当前时间
        criteria.andStartTimeLessThan(date);
        //结束时间  》= 当前的时间
        criteria.andEndTimeGreaterThanOrEqualTo(date);
        //select * from tb_seckill_goods where status=1 and  stock_count>0 and   (当前时间属于开始时间和结束时间之间)  start<curr<=end  and id not in (1,2,3)



        List<TbSeckillGoods> seckillGoods = redisTemplate.boundHashOps("seckillGoods").values();

        if(seckillGoods!=null && seckillGoods.size()>0) {
            List<Long> values = new ArrayList<>();
            for (TbSeckillGoods seckillGood : seckillGoods) {
                values.add(seckillGood.getId());
            }
            //排除redis中已有的商品的列表
            criteria.andIdNotIn(values);
        }

        List<TbSeckillGoods> tbSeckillGoods = seckillGoodsMapper.selectByExample(example);





        //2.将数据存储到redis中
        if(tbSeckillGoods!=null)
            for (TbSeckillGoods tbSeckillGood : tbSeckillGoods) {
                //将商品压入队列中  一个商品就是一个队列，队列的长度 和库存一样
                 pushQueueSeckillGoods(tbSeckillGood);
                 redisTemplate.boundHashOps("seckillGoods").put(tbSeckillGood.getId(),tbSeckillGood);
            }
    }

    private void pushQueueSeckillGoods(TbSeckillGoods tbSeckillGood){
        //循环库存
        for (Integer i = 0; i < tbSeckillGood.getStockCount(); i++) {
            redisTemplate.boundListOps(SysConstants.SEC_KILL_GOODS_PREFIX+tbSeckillGood.getId()).leftPush(tbSeckillGood.getId());
        }
    }
}
