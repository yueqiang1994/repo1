package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbAddress;
import com.pinyougou.user.service.AddressService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/address")
public class AddressController {

    @Reference
    private AddressService addressService;

    @RequestMapping("/findAddressList")
    public List<TbAddress> findAddressList(){
        //1.获取当前登录的用户的ID
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //2.调用地址服务的方法获取该用户的地址列表
        List<TbAddress> addressList = addressService.findAddressList(userId);
        return addressList;
    }

}
