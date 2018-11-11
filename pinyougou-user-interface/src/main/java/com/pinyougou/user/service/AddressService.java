package com.pinyougou.user.service;

import com.pinyougou.pojo.TbAddress;

import java.util.List;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.user.service
 * @since 1.0
 */
public interface AddressService {

    /**
     * 根据当前的登录的用户的ID 查询该用户下的所有的地址列表
     * @param userId
     * @return
     */
    public List<TbAddress> findAddressList(String userId);
}
