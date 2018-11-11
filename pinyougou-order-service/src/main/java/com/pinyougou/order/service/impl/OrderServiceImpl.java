package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.group.Cart;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.order.service.impl
 * @since 1.0
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbOrderMapper tbOrderMapper;

    @Autowired
    private TbOrderItemMapper orderItemMapper;

    @Autowired
    private TbPayLogMapper payLogMapper;
    @Override
    public void add(TbOrder order) {
        //需要拆单  循环遍历购物车列表  从redis中就直接可以获取到购物车列表
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
        double totalMoneyPaylog=0;
        //拆单
       List<String> orderListstr = new ArrayList<>();
        for (Cart cart : cartList) {
            //1.创建订单到订单表
            //1.1 生成一个订单号
            long orderId = new IdWorker(0, 0).nextId();

            orderListstr.add(orderId+"");
            TbOrder order1 = new TbOrder();
            //1.2 设置属性
            order1.setOrderId(orderId);
            order1.setPaymentType(order.getPaymentType());
            order1.setPostFee("0");//免邮费
            order1.setStatus("1");//未付款 状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价
            order1.setCreateTime(new Date());
            order1.setUpdateTime(order1.getCreateTime());
            order1.setUserId(order.getUserId());//contrller调用该服务方的时候先通过springseucirty来获取到当前的用户 设置到传递的order变量中

            order1.setReceiver(order.getReceiver());//收货人 页面做处理
            order1.setReceiverMobile(order.getReceiverMobile());//手机 页面处理
            order1.setReceiverAreaName(order.getReceiverAreaName());//收货人地址
            order1.setSourceType("2");//来源类型  这里可以在页面做处理
            order1.setSellerId(cart.getSellerId());//商家的ID

            //order1.setPayment();//总金额  应该设置的是每一个商家的卖的商品的总金额

            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            double money = 0;
            for (TbOrderItem orderItem : orderItemList) {
                money+=orderItem.getTotalFee().doubleValue();
                //2.创建订单项到订单项表
                orderItem.setId(new IdWorker(0,0).nextId());
                orderItem.setOrderId(orderId);//订单的ID
                orderItemMapper.insert(orderItem);
            }


            totalMoneyPaylog+=money;//累加买的所有的商家的总金额  yuan
            order1.setPayment(new BigDecimal(money));
            tbOrderMapper.insert(order1);
            //清空购物车
            redisTemplate.boundHashOps("cartList").delete(order.getUserId());


        }

        //添加一条支付日志记录  （未支付的状态，只有支付成才更新状态）
        TbPayLog payLog = new TbPayLog();
        payLog.setOutTradeNo(new IdWorker(0,2).nextId()+"");//支付订单号
        payLog.setCreateTime(new Date());
        double v = totalMoneyPaylog * 100;//0.01
        payLog.setTotalFee((long)v);//单位是分

        payLog.setUserId(order.getUserId());
        payLog.setTradeState("0");//未支付
        payLog.setPayType("1");//微信支付类型
        String orderListString = orderListstr.toString().replace("[", "").replace("]", "");
        payLog.setOrderList(orderListString);
        payLogMapper.insert(payLog);
        //对象放入redis中
        redisTemplate.boundHashOps(TbPayLog.class.getSimpleName()).put(order.getUserId(),payLog);


    }

    @Override
    public TbPayLog getTbPayLogFromRedis(String userId) {
        return (TbPayLog) redisTemplate.boundHashOps(TbPayLog.class.getSimpleName()).get(userId);
    }

    @Override
    public void updateTbPayLogAndTbOrder(String out_trade_no, String transaction_id) {
        //1.通过主键先获取到对象
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);

        //2.更新属性
        payLog.setPayTime(new Date());
        payLog.setTransactionId(transaction_id);//微信支付系统传递给payController ,再传递过来的这里
        payLog.setTradeState("1");//已支付

        payLogMapper.updateByPrimaryKey(payLog);//更新
        //3.获取订单的ID
        String orderList = payLog.getOrderList();//3,4

        String[] split = orderList.split(",");
        //更新用户下的订单的状态
        for (String orderId : split) {
            TbOrder tbOrder = tbOrderMapper.selectByPrimaryKey(Long.valueOf(orderId));
            tbOrder.setStatus("2");//已经支付
            tbOrder.setPaymentTime(payLog.getPayTime());
            tbOrderMapper.updateByPrimaryKey(tbOrder);
        }
        //4.删除redis中的支付日志
        redisTemplate.boundHashOps(TbPayLog.class.getSimpleName()).delete(payLog.getUserId());


    }

    public static void main(String[] args){
            //要的结果是 37,38
        System.out.println(TbPayLog.class.getSimpleName());
        List<String> orderListstr = new ArrayList<>();
        orderListstr.add("1");
        orderListstr.add("2");
        orderListstr.add("3");
        orderListstr.add("4");
        System.out.println(orderListstr);
        String s = orderListstr.toString().replace("[", "").replaceAll("]", "");
        System.out.println(s);
    }




}
