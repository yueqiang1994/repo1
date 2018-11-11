package com.pinyougou.seckill.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.seckill.controller
 * @since 1.0
 */
@Controller
public class PageLoginController {

    @RequestMapping("/page/login")
    public String gotoBack(String url){
          return "redirect:"+url;
    }
}
