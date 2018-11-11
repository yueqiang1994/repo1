package com.pinyougou.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.user.controller
 * @since 1.0
 */
@RequestMapping("/user/login")
@RestController
public class LoginUserInfoController {

    @RequestMapping("/info")
    public Map getInfo(){
        Map map = new HashMap();
        map.put("username", SecurityContextHolder.getContext().getAuthentication().getName());
        return map;
    }
}
