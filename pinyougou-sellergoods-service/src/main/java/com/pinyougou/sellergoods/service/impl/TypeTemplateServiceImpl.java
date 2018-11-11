package com.pinyougou.sellergoods.service.impl;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.common.pojo.SysConstants;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.pojo.TbTypeTemplateExample;
import com.pinyougou.pojo.TbTypeTemplateExample.Criteria;
import com.pinyougou.sellergoods.service.TypeTemplateService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page=   (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}

	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		//紧跟着的第一个查询才会被分页
		PageHelper.startPage(pageNum, pageSize);

		TbTypeTemplateExample example = new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();

		if (typeTemplate != null) {
			if (typeTemplate.getName() != null && typeTemplate.getName().length() > 0) {
				criteria.andNameLike("%" + typeTemplate.getName() + "%");
			}
			if (typeTemplate.getSpecIds() != null && typeTemplate.getSpecIds().length() > 0) {
				criteria.andSpecIdsLike("%" + typeTemplate.getSpecIds() + "%");
			}
			if (typeTemplate.getBrandIds() != null && typeTemplate.getBrandIds().length() > 0) {
				criteria.andBrandIdsLike("%" + typeTemplate.getBrandIds() + "%");
			}
			if (typeTemplate.getCustomAttributeItems() != null && typeTemplate.getCustomAttributeItems().length() > 0) {
				criteria.andCustomAttributeItemsLike("%" + typeTemplate.getCustomAttributeItems() + "%");
			}

		}

		Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(example);

		//将所有的模板的数据存储到 redis中  代码一定要在这里位置来使用。

		List<TbTypeTemplate> all = findAll();

		for (TbTypeTemplate tbTypeTemplate : all) {
			//品牌列表
			//[{"id":1,"text":"联想"}]
			String brandIds = tbTypeTemplate.getBrandIds();
			List<Map> mapList = JSON.parseArray(brandIds, Map.class);//{"id":1,"text":"联想"} ===Map
			redisTemplate.boundHashOps(SysConstants.SEARCH_REDIS_TYPE_TEMPLATE_BRAND_lIST_KEY).put(tbTypeTemplate.getId(),mapList);

			//缓存规格列表
			List<Map> specList = findSpecList(tbTypeTemplate.getId());
			redisTemplate.boundHashOps(SysConstants.SEARCH_REDIS_TYPE_TEMPLATE_SPEC_lIST_KEY).put(tbTypeTemplate.getId(),specList);
		}


		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private TbSpecificationOptionMapper optionMapper;

    @Override
    public List<Map> findSpecList(Long id) {
		TbTypeTemplate tbTypeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
		//获取规格的数据
		//[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
		String specIds = tbTypeTemplate.getSpecIds();
		//Map ==>{"id":27,"text":"网络"}
		List<Map> list= JSON.parseArray(specIds, Map.class);

		//页面需要的数据[{"id":27,"text":"网络",options:[{},{}]},{"id":32,"text":"机身内存"}]
		for (Map map : list) {
			//获取id  根据ID 获取选项列表 拼接
			Integer idi = (Integer) map.get("id");

			//select * from tb_specification_option where spec_id=27
			TbSpecificationOptionExample exmaple = new TbSpecificationOptionExample();
			exmaple.createCriteria().andSpecIdEqualTo(Long.valueOf(idi));
			List<TbSpecificationOption> options = optionMapper.selectByExample(exmaple);
			map.put("options",options);
		}
		return list;
    }

}
