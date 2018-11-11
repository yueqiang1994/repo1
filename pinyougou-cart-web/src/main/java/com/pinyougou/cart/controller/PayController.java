package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WxPayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.cart.controller
 * @since 1.0
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WxPayService payService;

    @Reference
    private OrderService orderService;

    @RequestMapping("/createNative")
    public Map createNative(){
       /* //1.生成支付订单的ID
        String out_trade_no = new IdWorker(0, 1).nextId() + "";
        //2.写死金额 1*/

        TbPayLog tbPayLogFromRedis = orderService.getTbPayLogFromRedis(SecurityContextHolder.getContext().getAuthentication().getName());
        if(tbPayLogFromRedis!=null) {
            Map map = payService.createNative(tbPayLogFromRedis.getOutTradeNo(), tbPayLogFromRedis.getTotalFee() + "");
            return map;
        }
        return new HashMap();
    }
    //查询支付的状态
    @RequestMapping("/queryStatus")
    public Result queryStatus(String out_trade_no){
        Result result = new Result(false,"支付失败");
        int x =0;
        while(true) {
            //隔一个3秒钟时间查询一次
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;

            //如果超过5分钟就是调用超时了

            if(x>=10){
               result=  new Result(false,"支付超时");
               break;
            }


            Map map = payService.queryStatus(out_trade_no);
            if (map == null) {
                result= new Result(false, "支付失败");
                break;
            }
            //判断支付的状态  如果支付成功
            if ("SUCCESS".equals(map.get("trade_state"))) {

                // 更新用户下的订单的状态 更新该用户下的支付日志的状态 ，更新微信支付的transaction_id ，删除redis中的支付日志
                orderService.updateTbPayLogAndTbOrder(out_trade_no,(String) map.get("transaction_id"));
                result =  new Result(true, "支付成功");
                break;
            }
        }
        return result;
    }
}
