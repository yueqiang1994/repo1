package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.common.util.CookieUtil;
import com.pinyougou.group.Cart;
import entity.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.cart.controller
 * @since 1.0
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    /**
     *
     * @param itemId 要添加的商品的ID
     * @param num 要添加的商品的数量
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    //这个注解如果修饰的方法，那么就只针对该方法起作用，如果放在类上边 ，就表示所有的方法都支持跨域
    @CrossOrigin(origins = {"http://localhost:9105"},allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId, Integer num, HttpServletRequest request, HttpServletResponse response){

        //表示服务器 允许跨域请求 （允许指定的：http://localhost:9105 域的访问）
        //response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        //表示如果涉及到cookie的操作，需要服务器同意 跨域发送cookie的数据过来
        //response.setHeader("Access-Control-Allow-Credentials", "true");
        try {
            //1.判断用户是否已经登录
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            //anonymousUser
            System.out.println("name:>>>>"+name);
            if("anonymousUser".equals(name)) {
                //2.如果用户没有登录 操作的是 cookie
                //2.1 首先先从cookie中获取到已有的购物车列表
                String cartListstr = CookieUtil.getCookieValue(request, "cartList", true);
                List<Cart> cartList = new ArrayList<>();
                if(StringUtils.isNotBlank(cartListstr)) {
                    //2.2 调用service的方法 （向已有的购物车添加商品） 返回一个最新的购物车列表
                    cartList = JSON.parseArray(cartListstr, Cart.class);
                }
                List<Cart> cartsnew = cartService.addGoodsToCartList(cartList, itemId, num);
                String jsonStringcart = JSON.toJSONString(cartsnew);
                //2.3 将最新的购物车列表 重新存储到cookie中
                CookieUtil.setCookie(request,response,"cartList",jsonStringcart,7*24*3600,true);

            }else {
                //3.如果用户已经登录 操作的redis
                //3.1 先从redis中获取已有的购物车列表
                List<Cart> cartListFromRedis = cartService.getCartListFromRedis(name);
                //3.2 向已有的购物车列表中添加商品(调用service的方法 )  返回一个最新的购物车的列表
                List<Cart> cartsnew = cartService.addGoodsToCartList(cartListFromRedis, itemId, num);
                //3.3 重新将最新的数据存储到redis中
                cartService.saveCartListToRedis(name,cartsnew);
            }
            return new Result(true,"添加购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加购物车失败");
        }

    }

    @RequestMapping("/findCartList")
    public List<Cart> findCartList(HttpServletRequest request,HttpServletResponse response){
        //1.判断用户是否已经登录
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        if("anonymousUser".equals(name)) {
           //直接从cookie中获取购物车列表
            String cartListstr = CookieUtil.getCookieValue(request, "cartList", true);
            List<Cart> cartList = new ArrayList<>();
            if(StringUtils.isNotBlank(cartListstr)) {
                //2.2 调用service的方法 （向已有的购物车添加商品） 返回一个最新的购物车列表
                cartList = JSON.parseArray(cartListstr, Cart.class);
            }
            return cartList;
        }else{
            //3从redis中获取购物车列表


            //4合并cookie中的购物车到redis中

            // 4.1 先从cookie中获取到购物车列表
            String cartListstr = CookieUtil.getCookieValue(request, "cartList", true);
            List<Cart> cartList = new ArrayList<>();
            if(StringUtils.isNotBlank(cartListstr)) {
                //2.2 调用service的方法 （向已有的购物车添加商品） 返回一个最新的购物车列表
                cartList = JSON.parseArray(cartListstr, Cart.class);
            }

            // 4.2 再从redis中获取到购物车列表
            //cartListFromRedis
            List<Cart> cartListFromRedis = cartService.getCartListFromRedis(name);

            //4.3 合并 （） 获取到最新的购物车列表
            List<Cart> cartsnew = cartService.mergerCartList(cartList, cartListFromRedis);

            //4.4 将最新的购物车列表 存储到redis中
            cartService.saveCartListToRedis(name,cartsnew);
            //4.5 cookie中的购物车列表 清空
            CookieUtil.deleteCookie(request,response,"cartList");
//            List<Cart> cartListFromRedis1 = cartService.getCartListFromRedis(name);

            return cartsnew;
        }

    }
}
