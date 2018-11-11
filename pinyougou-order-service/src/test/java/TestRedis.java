import com.pinyougou.pojo.TbPayLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package PACKAGE_NAME
 * @since 1.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring/applicationContext-redis.xml")
public class TestRedis {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void test(){

        TbPayLog zhangsanfeng = (TbPayLog) redisTemplate.boundHashOps(TbPayLog.class.getSimpleName()).get("zhangsanfeng");
        System.out.println(zhangsanfeng.getTotalFee());


    }
}
