package com.pinyougou.shop.security;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sound.midi.Soundbank;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义的认证类 来实现自定义的业务逻辑
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.shop.security
 * @since 1.0
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //需要自己定义自己的业务逻辑
        System.out.println(">>>"+username);

        //1.从页面中获取商家 根据商家的名称从该数据库中获取商家对象
        TbSeller seller = sellerService.findOne(username);
        if(seller==null) {
            //2.如果没有  表示认证失败
            return null;
        }

        if(!"1".equals(seller.getStatus())) {
            //3.如果有商家对象   判断 该商家是否已经审核通过了  如果没通过  表示认证失败
            return null;
        }
        //4.如果 已经通过了 就需要将数据库中的密码获取到 传递给User 对象，此时 spring security框架会自动的进行页面中的密码匹配

        //第一个参数：从该页面中获取的用户名
        //第二个参数：从数据库中获取的用户名对应的密码
        //第三个参数：该用户拥有的权限
        List<SimpleGrantedAuthority> list = new ArrayList<>();
        list.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        //spring security 会自动的讲数据库中的密码和页面传递过来的密码进行匹配，如果一致就登录成，如果失败就抛出异常，认证失败
//        return new User(username,"123456", AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_SELLER"));
        return new User(username,seller.getPassword(),list);
    }

    public static void main(String[] args){
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode("123456");
        String encode1 = bCryptPasswordEncoder.encode("123456");
        System.out.println(encode);
        System.out.println(encode1);
    }
}
