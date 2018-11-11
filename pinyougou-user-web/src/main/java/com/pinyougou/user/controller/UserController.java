package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.util.PhoneFormatCheckUtils;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.user.controller
 * @since 1.0
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Reference
    private UserService userService;

    @RequestMapping("/add")
    public Result add(@RequestBody  TbUser user,String code){
        try {
            boolean phoneLegal = PhoneFormatCheckUtils.isPhoneLegal(user.getPhone());
            if(!phoneLegal){
                return new Result(false,"你输入的不是手机号");
            }

            //先验证是否是正确的验证码，如果不正确 不能注册
            boolean flag = userService.isChecked(code,user.getPhone());
            if(!flag){//验证没通过
                return new Result(false,"验证码错误");
            }
            userService.add(user);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }

    @RequestMapping("/createSms")
    public Result createSms(String phone){
        try {
            //校验
            boolean phoneLegal = PhoneFormatCheckUtils.isPhoneLegal(phone);
            if(!phoneLegal){
                return new Result(false,"你输入的不是手机号");
            }
            userService.createSms(phone);
            return new Result(true,"你看看手机");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"发送失败");
        }
    }
}
