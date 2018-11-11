package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.seckill.controller
 * @since 1.0
 */
@RestController
@RequestMapping("/seckillOrder")
public class SeckillOrderController {


    @Reference
    private SeckillOrderService seckillOrderService;

    /**
     *
     * @param id 秒杀下单的时候的秒杀商品的ID
     * @return
     */
    @RequestMapping("/submitOrder")
    public Result submitOrder(Long id){
        //调用服务的方法执行秒杀下单
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if(userId.equals("anonymousUser")){
            return new Result(false,"401");//要登录
        }
        try {

            seckillOrderService.addOrder(id,userId);
            return new Result(true,"创建订单成功");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());//e.getMessage就是商品已售罄

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"创建订单失败");
        }
    }

    //页面每隔三秒中调用一次请求 查询订单的状态  ：用户在排队 就提示排对，如果是成功就是提示成功
    @RequestMapping("/queryStatus")
    public Result queryStatus(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if(userId.equals("anonymousUser")){
            return new Result(false,"401");//表示要登录
        }
        //从redis中获取该用户的订单，如果能获取到说明已经创建成功了
        TbSeckillOrder fromRedis = seckillOrderService.getFromRedis(userId);
        if(fromRedis!=null){
            return new Result(true,"创建成功");
        }else{
            //能直接判断一定是创建订单失败吗？不一定，因为有可能这个用户在排队。
            boolean flag = seckillOrderService.isFlag(userId);
            if(flag){
                //在排队
                return new Result(false,"正在排队中");
            }else {
                return new Result(false,"支付失败");
            }
        }
    }
}
