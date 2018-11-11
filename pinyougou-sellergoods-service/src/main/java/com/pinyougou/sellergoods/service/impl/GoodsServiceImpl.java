package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.group.Goods;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import org.csource.common.MyException;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
//@Transactional//表示所有的方法都要被事务管理
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbItemCatMapper catMapper;

	@Autowired
	private TbSellerMapper sellerMapper;

	@Autowired
	private TbBrandMapper brandMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
//	@Transactional(rollbackFor = Exception.class)//如果出现Exception包括它子类异常，就回滚
	public void add(Goods goods) {
		TbGoods tbGoods = goods.getGoods();//SPU
		tbGoods.setAuditStatus("0");//未审核
		tbGoods.setIsDelete(false);//设置为未删除
		//
		TbGoodsDesc goodsDesc = goods.getGoodsDesc();
		List<TbItem> itemList = goods.getItemList();
		//1.添加商品SPU的数据  mybatis中的主键返回
		goodsMapper.insert(tbGoods);
		//2.添加商品描述的数据
		goodsDesc.setGoodsId(tbGoods.getId());
		goodsDescMapper.insert(goodsDesc);
		//3.添加商品的SKU的列表
		if("1".equals(tbGoods.getIsEnableSpec())){
			itemList = goods.getItemList();
			for (TbItem tbItem : itemList) {
				//补全

				//设置商品的标题（SPU名称 规格选项）
				String title = tbGoods.getGoodsName();
				String spec = tbItem.getSpec();//{"网络":"移动4G"}
				Map<String,String> specMap = JSON.parseObject(spec, Map.class);
				for (String key : specMap.keySet()) {
					title+=" "+specMap.get(key);
				}
				tbItem.setTitle(title);

				//设置图片  从SPU中获取一张即可
				String itemImages = goodsDesc.getItemImages();//[{color:"",url:""}]

				List<Map> mapList = JSON.parseArray(itemImages, Map.class);
				tbItem.setImage(mapList.get(0).get("url").toString());

				//设置商品分类
				TbItemCat tbItemCat = catMapper.selectByPrimaryKey(tbGoods.getCategory3Id());

				tbItem.setCategoryid(tbItemCat.getId());
				tbItem.setCategory(tbItemCat.getName());

				tbItem.setCreateTime(new Date());
				tbItem.setUpdateTime(tbItem.getCreateTime());

				tbItem.setGoodsId(tbGoods.getId());

				//查询商家和店铺名
				TbSeller seller = sellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
				tbItem.setSellerId(seller.getSellerId());
				tbItem.setSeller(seller.getNickName());//店铺名


				//品牌
				TbBrand tbBrand = brandMapper.selectByPrimaryKey(tbGoods.getBrandId());
				tbItem.setBrand(tbBrand.getName());
				itemMapper.insert(tbItem);
			}
		}else{
			//单品
			TbItem tbItem = new TbItem();

			//补全

			tbItem.setTitle(tbGoods.getGoodsName());

			tbItem.setPrice(tbGoods.getPrice());
			tbItem.setNum(9999);
			tbItem.setStatus("1");
			tbItem.setCreateTime(new Date());
			tbItem.setUpdateTime(tbItem.getCreateTime());
			tbItem.setIsDefault("1");//
			tbItem.setSpec("{}");//空对象字符串
			//设置图片  从SPU中获取一张即可
			String itemImages = goodsDesc.getItemImages();//[{color:"",url:""}]

			List<Map> mapList = JSON.parseArray(itemImages, Map.class);
			tbItem.setImage(mapList.get(0).get("url").toString());

			//设置商品分类
			TbItemCat tbItemCat = catMapper.selectByPrimaryKey(tbGoods.getCategory3Id());

			tbItem.setCategoryid(tbItemCat.getId());
			tbItem.setCategory(tbItemCat.getName());

			tbItem.setCreateTime(new Date());
			tbItem.setUpdateTime(tbItem.getCreateTime());

			tbItem.setGoodsId(tbGoods.getId());

			//查询商家和店铺名
			TbSeller seller = sellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
			tbItem.setSellerId(seller.getSellerId());
			tbItem.setSeller(seller.getNickName());//店铺名


			//品牌
			TbBrand tbBrand = brandMapper.selectByPrimaryKey(tbGoods.getBrandId());
			tbItem.setBrand(tbBrand.getName());


			itemMapper.insert(tbItem);

		}
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//修改 商品的SPU的数据
		TbGoods goods1 = goods.getGoods();
		goodsMapper.updateByPrimaryKey(goods1);
		//修改商品的描述数据
		TbGoodsDesc goodsDesc = goods.getGoodsDesc();

		goodsDescMapper.updateByPrimaryKey(goodsDesc);


		List<TbItem> itemList= goods.getItemList();

		//修改商品的SKU列表数据


		//先删除原来的 在添加从页面传递过来的现有的商品的列表
		//delete from tb_item where goods_id =1
		TbItemExample example = new TbItemExample();
		example.createCriteria().andGoodsIdEqualTo(goods1.getId());
		itemMapper.deleteByExample(example);

		//再添加

		if("1".equals(goods1.getIsEnableSpec())){
			itemList = goods.getItemList();
			for (TbItem tbItem : itemList) {
				//补全

				//设置商品的标题（SPU名称 规格选项）
				String title = goods1.getGoodsName();
				String spec = tbItem.getSpec();//{"网络":"移动4G"}
				Map<String,String> specMap = JSON.parseObject(spec, Map.class);
				for (String key : specMap.keySet()) {
					title+=" "+specMap.get(key);
				}
				tbItem.setTitle(title);

				//设置图片  从SPU中获取一张即可
				String itemImages = goodsDesc.getItemImages();//[{color:"",url:""}]

				List<Map> mapList = JSON.parseArray(itemImages, Map.class);
				tbItem.setImage(mapList.get(0).get("url").toString());

				//设置商品分类
				TbItemCat tbItemCat = catMapper.selectByPrimaryKey(goods1.getCategory3Id());

				tbItem.setCategoryid(tbItemCat.getId());
				tbItem.setCategory(tbItemCat.getName());

				tbItem.setCreateTime(new Date());
				tbItem.setUpdateTime(tbItem.getCreateTime());

				tbItem.setGoodsId(goods1.getId());

				//查询商家和店铺名
				TbSeller seller = sellerMapper.selectByPrimaryKey(goods1.getSellerId());
				tbItem.setSellerId(seller.getSellerId());
				tbItem.setSeller(seller.getNickName());//店铺名


				//品牌
				TbBrand tbBrand = brandMapper.selectByPrimaryKey(goods1.getBrandId());
				tbItem.setBrand(tbBrand.getName());
				itemMapper.insert(tbItem);
			}
		}else{
			//单品
			TbItem tbItem = new TbItem();

			//补全

			tbItem.setTitle(goods1.getGoodsName());

			tbItem.setPrice(goods1.getPrice());
			tbItem.setNum(9999);
			tbItem.setStatus("1");
			tbItem.setCreateTime(new Date());
			tbItem.setUpdateTime(tbItem.getCreateTime());
			tbItem.setIsDefault("1");//
			tbItem.setSpec("{}");//空对象字符串
			//设置图片  从SPU中获取一张即可
			String itemImages = goodsDesc.getItemImages();//[{color:"",url:""}]

			List<Map> mapList = JSON.parseArray(itemImages, Map.class);
			tbItem.setImage(mapList.get(0).get("url").toString());

			//设置商品分类
			TbItemCat tbItemCat = catMapper.selectByPrimaryKey(goods1.getCategory3Id());

			tbItem.setCategoryid(tbItemCat.getId());
			tbItem.setCategory(tbItemCat.getName());

			tbItem.setCreateTime(new Date());
			tbItem.setUpdateTime(tbItem.getCreateTime());

			tbItem.setGoodsId(goods1.getId());

			//查询商家和店铺名
			TbSeller seller = sellerMapper.selectByPrimaryKey(goods1.getSellerId());
			tbItem.setSellerId(seller.getSellerId());
			tbItem.setSeller(seller.getNickName());//店铺名
			//品牌
			TbBrand tbBrand = brandMapper.selectByPrimaryKey(goods1.getBrandId());
			tbItem.setBrand(tbBrand.getName());
			itemMapper.insert(tbItem);

		}


	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		//查询SPU的数据
		TbGoods goods1 = goodsMapper.selectByPrimaryKey(id);
		//查询SPU的描述信息
		TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		//查询SKU的列表
		//select * from tb_item where goods_id=123456
		TbItemExample example = new TbItemExample();
		example.createCriteria().andGoodsIdEqualTo(id);
		List<TbItem> tbItems = itemMapper.selectByExample(example);

		goods.setGoods(goods1);
		goods.setGoodsDesc(goodsDesc);
		goods.setItemList(tbItems);

		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		//update set is_delete = 1 where id in (1,2,3)

		for(Long id:ids){
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete(true);
			goodsMapper.updateByPrimaryKey(goods);
//			goodsMapper.deleteByPrimaryKey(id);//物理删除
		}

	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteEqualTo(false);//过滤删除的 只展示没有删除的
		if(goods!=null){			
			if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusEqualTo(goods.getAuditStatus());
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecEqualTo(goods.getIsEnableSpec());
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

    @Override
    public void updateStatus(Long[] ids, String status) {
		//update tb_goods set audit_status=stauts where id in (1,2,3)
		for (Long id : ids) {
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(goods);
		}

	}

    @Override
    public List<TbItem> selectItemListByIds(Long[] ids) {
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");//正常的商品
		criteria.andGoodsIdIn(Arrays.asList(ids));
		List<TbItem> tbItems = itemMapper.selectByExample(example);//select * from tb_item where goods_id in (1,2,3) and status=1
		return tbItems;
    }

}
