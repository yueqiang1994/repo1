package com.pinyougou.user.service;

import com.pinyougou.pojo.TbUser;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.user.service
 * @since 1.0
 */
public interface UserService {

    //添加一个用户
    public void add(TbUser user);

    /**
     * 根据手机号 来生成短信验证码 6位数字
     * @param phone
     */
    public void createSms(String phone);

    /**
     *
     * @param code  是页面传递过来的用户填写的验证码
     * @param phone  手机号
     * @return
     */
    boolean isChecked(String code, String phone);



}
