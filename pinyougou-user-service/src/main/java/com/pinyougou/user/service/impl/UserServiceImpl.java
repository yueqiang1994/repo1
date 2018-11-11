package com.pinyougou.user.service.impl;

import com.alibaba.druid.util.DaemonThreadFactory;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbUserMapper;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.jms.Destination;
import java.sql.ParameterMetaData;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.user.service.impl
 * @since 1.0
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private TbUserMapper userMapper;
    @Override
    public void add(TbUser user) {
        //1.补全其他的属性的值
        user.setCreated(new Date());
        user.setUpdated(user.getCreated());

        //2.密码要进行加密 MD5
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        userMapper.insert(user);
        //清空缓存
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Resource(name="pinyousms")
    private Destination destination;

    @Value("${template_code}")
    private String template_code;
    @Value("${sign_name}")
    private String sign_name;







    @Override
    public void createSms(String phone) {
        //1.生成一个随机的6位数字 验证码
        double random = Math.random();
        double rand1 = random*1000000;
        long code = (long)rand1;
        System.out.println(code);

        //2.先存储验证码 到redis中 并且设置 过期时间。
        redisTemplate.boundValueOps("PINYOUGOU_SMS_:"+phone).set(code+"");
        //设置过期时间
        redisTemplate.boundValueOps("PINYOUGOU_SMS_:"+phone).expire(60, TimeUnit.SECONDS);

        //3.发送消息
        Map<String,String> map = new HashMap<>();
        map.put("mobile",phone);
        map.put("template_code",template_code);
        map.put("sign_name",sign_name);
       /* Map param = new HashMap();
        param.put("code",code);
        String s = JSON.toJSONString(param);*/

        map.put("param","{\"code\":\""+code+"\"}");

        //map.put("param",s);
        jmsTemplate.convertAndSend(destination,map);



    }

    @Override
    public boolean isChecked(String code, String phone) {
        String codefromredis = (String) redisTemplate.boundValueOps("PINYOUGOU_SMS_:" + phone).get();
        //先做错误的判断 直接返回
        if(StringUtils.isNotBlank(codefromredis)){
            //在比较是否相当
            if(codefromredis.equals(code)){
                return true;
            }
        }
        return false;
    }


}
