package com.pinyougou.seckill.service;

import com.pinyougou.pojo.TbSeckillGoods;

import java.util.List;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.seckill.service
 * @since 1.0
 */
public interface SeckillGoodsService {

    public List<TbSeckillGoods> findList();

    public TbSeckillGoods findOne(Long id);
}
