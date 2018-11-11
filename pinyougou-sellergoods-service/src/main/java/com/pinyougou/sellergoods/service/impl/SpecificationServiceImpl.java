package com.pinyougou.sellergoods.service.impl;

import java.util.*;

import com.pinyougou.group.Specification;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.pojo.TbSpecificationExample.Criteria;
import com.pinyougou.sellergoods.service.SpecificationService;

import entity.PageResult;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private TbSpecificationMapper specificationMapper;

    @Autowired
    private TbSpecificationOptionMapper optionMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbSpecification> findAll() {
        return specificationMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(Specification specification) {
        TbSpecification tbSpecification = specification.getSpecification();
        //1.插入规格的数据
        specificationMapper.insert(tbSpecification);//
        //2.在插入了规格的数据之后获取该ID  自增主键返回
        Long specId = tbSpecification.getId();
        //3.插入规格的选项的数据（需要用到规格的ID）
        List<TbSpecificationOption> optionList = specification.getOptionList();
        for (TbSpecificationOption option : optionList) {
            //设置规格的ID
            option.setSpecId(specId);
            optionMapper.insert(option);
        }


    }


    /**
     * 修改
     */
    @Override
    public void update(Specification specification) {
        TbSpecification tbSpecification = specification.getSpecification();
        //1.修改规格的数据
        specificationMapper.updateByPrimaryKey(tbSpecification);

        //2.修改规格的选项的数据
        List<TbSpecificationOption> optionList = specification.getOptionList();
        //先删除原来的数据

        //delete from tboption where specid=1
        TbSpecificationOptionExample exmaple = new TbSpecificationOptionExample();
        exmaple.createCriteria().andSpecIdEqualTo(tbSpecification.getId());
        optionMapper.deleteByExample(exmaple);

        //再添加最新的数据
        for (TbSpecificationOption option : optionList) {
            //option.setSpecId(tbSpecification.getId());
            optionMapper.insert(option);
        }

    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Specification findOne(Long id) {
        // 定义个一个specification
        Specification specification = new Specification();

        // 查询规格的数据
        TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);

        // 查询规格的选项的数据LIst
        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        example.createCriteria().andSpecIdEqualTo(id);
        List<TbSpecificationOption> options = optionMapper.selectByExample(example);//select * from tbspecification_option where specid=1
        //返回
        specification.setSpecification(tbSpecification);
        specification.setOptionList(options);
        return specification;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            //删除规格
            specificationMapper.deleteByPrimaryKey(id);
            //删除规格的选项
        }

        //delete from option where specid in (1,2,3)
        TbSpecificationOptionExample exmaple = new TbSpecificationOptionExample();
        exmaple.createCriteria().andSpecIdIn(Arrays.asList(ids));
        optionMapper.deleteByExample(exmaple);
    }


    @Override
    public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSpecificationExample example = new TbSpecificationExample();
        Criteria criteria = example.createCriteria();

        if (specification != null) {
            if (specification.getSpecName() != null && specification.getSpecName().length() > 0) {
                criteria.andSpecNameLike("%" + specification.getSpecName() + "%");
            }

        }

        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public List<Map> findOptionList() {
        List<Map> mapList = new ArrayList<>();
        List<TbSpecification> specifications = specificationMapper.selectByExample(null);
        for (TbSpecification tbSpecification : specifications) {
            Map map = new HashMap();
            map.put("id", tbSpecification.getId());
            map.put("text", tbSpecification.getSpecName());
            mapList.add(map);
        }
        return mapList;
    }

}
