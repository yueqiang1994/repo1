package com.pinyougou.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.test
 * @since 1.0
 */
@ContextConfiguration("classpath:spring/applicationContext-redis.xml")
@RunWith(SpringRunner.class)
public class RedisTempalteTest {

    //1.String

    @Autowired
    private RedisTemplate redisTemplate;
    //
    @Test
    public void  valueAdd(){
        //set key11 value11
        //key 和value的值  对象一定要序列化对象才可以。
        redisTemplate.boundValueOps("key111").set("value111");
        //获取值
        Object value11 = redisTemplate.boundValueOps("key111").get();
        System.out.println(value11);
    }

    //2.hash

    @Test
    public void hashAdd(){
        redisTemplate.boundHashOps("hashbigkey").put("field1","value1");
        redisTemplate.boundHashOps("hashbigkey").put("field2","value2");

        System.out.println( redisTemplate.boundHashOps("hashbigkey").get("field1"));
        System.out.println(redisTemplate.boundHashOps("hashbigkey").values());

    }

    //3.list

    //4.set

    //5.zset

}
