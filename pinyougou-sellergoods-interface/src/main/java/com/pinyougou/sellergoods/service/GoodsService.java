package com.pinyougou.sellergoods.service;
import java.util.List;

import com.pinyougou.group.Goods;
import com.pinyougou.pojo.TbGoods;

import com.pinyougou.pojo.TbItem;
import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface GoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加 组合对象（SPU  SKU  描述）
	*/
	public void add(Goods goods);
	
	
	/**
	 * 修改
	 */
	public void update(Goods goods);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public Goods findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize);

	/*
		批量更新状态（审核 或者驳回..）
	 */
	public void updateStatus(Long[] ids,String status);//sttus=1


	/**
	 * 根据SPU的ID数组查询出该SPU下的所有的SKU列表
	 * @param ids
	 * @return
	 */
	public List<TbItem> selectItemListByIds(Long[] ids);


}
