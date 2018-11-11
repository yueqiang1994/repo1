package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.group.Cart;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.cart.service.impl
 * @since 1.0
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> orgincartList, Long itemId, Integer num) {
        //1.根据商品的ID 获取到商品的数据
        TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);
        //2.获取该商品的商家的ID
        String sellerId = tbItem.getSellerId();
        Cart cart = searchCartBySellerId(sellerId,orgincartList);
        //3.判断 该商品的所属的商家 是否在购物车列表中存在，
        // 如果没有存在，说明你没有买过该商家的商品  （要直接添加商品了）
        // 如果存在，说明你在这个商家买过商品
        if(cart!=null){
            //说明你在这个商家买过商品
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            //4. 判断 要添加的商品 是否在该商家的明细列表中存在
            TbOrderItem orderItem = searchOrderItemByItemId(itemId,orderItemList);
            if(orderItem!=null){
                    //找到啦，说明之前买个这个商品 现在又要加：数量相加即可
                    orderItem.setNum(orderItem.getNum()+num);
                    //重新计算总金额
                    double v = orderItem.getNum() * (orderItem.getPrice().doubleValue());
                    orderItem.setTotalFee(new BigDecimal(v));

                    if(orderItem.getNum()<=0){
                        orderItemList.remove(orderItem);
                    }
                    //如果删除完了 就应该删除这个Cart====代表的就是该商家
                    if(orderItemList.size()==0){
                        orgincartList.remove(cart);
                    }
            }else{
                    //说明没找到，说明在商家买过其他的商品 ，现在要添加一个新的商品
                TbOrderItem orderItemnew = new TbOrderItem();
                orderItemnew.setTitle(tbItem.getTitle());
                orderItemnew.setPrice(tbItem.getPrice());
                orderItemnew.setNum(num);//
                //计算金额（买个商品的单价* 数量）
                double v = orderItemnew.getPrice().doubleValue() * orderItemnew.getNum();
                orderItemnew.setTotalFee(new BigDecimal(v));
                orderItemnew.setPicPath(tbItem.getImage());
                orderItemnew.setItemId(itemId);
                orderItemnew.setGoodsId(tbItem.getGoodsId());
                orderItemnew.setSellerId(sellerId);//卖家
                orderItemList.add(orderItemnew);
            }

        }else{
            //说明你没有买过该商家的商品  （要直接添加商品了）
           cart = new Cart();//

           cart.setSellerId(sellerId);
           cart.setSellerName(tbItem.getSeller());//商家名称（格力旗舰店）


            List<TbOrderItem> orderitemlist = new ArrayList<>();

            //说明没找到，说明在商家买过其他的商品 ，现在要添加一个新的商品
            TbOrderItem orrderItemnewnew = new TbOrderItem();
            orrderItemnewnew.setTitle(tbItem.getTitle());
            orrderItemnewnew.setPrice(tbItem.getPrice());
            orrderItemnewnew.setNum(num);//
            //计算金额（买个商品的单价* 数量）
            double v = orrderItemnewnew.getPrice().doubleValue() * orrderItemnewnew.getNum();
            orrderItemnewnew.setTotalFee(new BigDecimal(v));
            orrderItemnewnew.setPicPath(tbItem.getImage());
            orrderItemnewnew.setItemId(itemId);
            orrderItemnewnew.setGoodsId(tbItem.getGoodsId());
            orrderItemnewnew.setSellerId(sellerId);//卖家

            orderitemlist.add(orrderItemnewnew);


            cart.setOrderItemList(orderitemlist); //买的所属的商家的商品列表
            orgincartList.add(cart);
        }
        return orgincartList;
    }

    @Override
    public void saveCartListToRedis(String userId, List<Cart> cartList) {
        redisTemplate.boundHashOps("cartList").put(userId,cartList);
    }

    @Override
    public List<Cart> getCartListFromRedis(String userId) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(userId);

        if(cartList==null){
            return new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public List<Cart> mergerCartList(List<Cart> cookieCartList, List<Cart> redisCartList) {
        for (Cart cart : cookieCartList) {
            List<TbOrderItem> orderItemList = cart.getOrderItemList();//商品列表
            for (TbOrderItem orderItem : orderItemList) {
                Long itemId = orderItem.getItemId();//cookie中的商品的ID
                Integer num = orderItem.getNum(); //cookie中的商品的购买数量

                redisCartList = addGoodsToCartList(redisCartList, itemId, num);
            }
        }
        //最新的购物车列表
        return redisCartList;
    }

    private TbOrderItem searchOrderItemByItemId(Long itemId, List<TbOrderItem> orderItemList) {
        for (TbOrderItem orderItem : orderItemList) {
            if(itemId.longValue()==orderItem.getItemId().longValue()){//说明就是这个商品
                //找到了该商品
                return orderItem;
            }
        }
        return null;
    }

    private Cart searchCartBySellerId(String sellerId, List<Cart> orgincartList) {
        for (Cart cart : orgincartList) {
           if (cart.getSellerId().equals(sellerId)){
               return cart;
           }
        }
        return null;
    }
}
