package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillGoodsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.seckill.controller
 * @since 1.0
 */
@RequestMapping("/seckillGoods")
@RestController
public class SeckillGoodsController {
    @Reference
    private SeckillGoodsService seckillGoodsService;

    @RequestMapping("/findList")
    public List<TbSeckillGoods> findList(){
        return seckillGoodsService.findList();
    }

    @RequestMapping("/findOne")
    public TbSeckillGoods findOne(Long id){
        return seckillGoodsService.findOne(id);
    }
}
