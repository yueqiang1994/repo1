package com.pinyougou.cart.service;

import com.pinyougou.group.Cart;

import java.util.List;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.cart.service
 * @since 1.0
 */
public interface CartService {

    //向已有的购物车列表中添加 商品

    /**
     *
     * @param orgincartList  原有的购物车列表
     * @param itemId  要添加的商品的ID
     * @param num  要添加的数量
     * @return  最新的购物车列表
     */
    public List<Cart> addGoodsToCartList(List<Cart> orgincartList,Long itemId,Integer num);


    /**
     *
     * @param userId  当前登录的用户
     * @param cartList  当前登录用户的购物车列表
     */
    public void saveCartListToRedis(String userId,List<Cart> cartList);


    /**
     * 获取当前登录的用户的对应的购物车列表
     * @param userId
     * @return
     */
    public List<Cart> getCartListFromRedis(String userId);

    /**
     *  将cookie中的购物车列表 合并到 redis中的购物车列表去 返回最新的。
     * @param cookieCartList  cookie中的购物车列表
     * @param redisCartList   redis中的购物车列表
     * @return
     */
    public List<Cart> mergerCartList(List<Cart> cookieCartList,List<Cart> redisCartList);


}
