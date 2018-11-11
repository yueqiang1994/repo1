package com.pinyougou.content.service.impl;

import java.util.List;

import com.pinyougou.content.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private TbContentMapper contentMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbContent> findAll() {
        return contentMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbContent content) {
        //清空缓存
        redisTemplate.boundHashOps("contentList").delete(content.getCategoryId());
        contentMapper.insert(content);
    }


    /**
     * content  是从页面传递过来的
     */
    @Override
    public void update(TbContent content) {
        //清空缓存
        //1.获取原来的分类的ID
        TbContent tbContent = contentMapper.selectByPrimaryKey(content.getId());
        Long categoryId = tbContent.getCategoryId();//原来的


        //2.获取现有的分类的ID 页面传递过来的
        Long categoryId1 = content.getCategoryId();

        //3.判断是否一致  如果不一致 删除两个  如果一致 删除一个

        if(categoryId!=categoryId1.longValue()){
            redisTemplate.boundHashOps("contentList").delete(categoryId);
            redisTemplate.boundHashOps("contentList").delete(categoryId1);
        }else{
            redisTemplate.boundHashOps("contentList").delete(categoryId1);
        }
        contentMapper.updateByPrimaryKey(content);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbContent findOne(Long id) {
        return contentMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        //清空缓存
        for (Long id : ids) {
            TbContent tbContent = contentMapper.selectByPrimaryKey(id);
            redisTemplate.boundHashOps("contentList").delete(tbContent.getCategoryId());
            contentMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbContent content, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbContentExample example = new TbContentExample();
        Criteria criteria = example.createCriteria();

        if (content != null) {
            if (content.getTitle() != null && content.getTitle().length() > 0) {
                criteria.andTitleLike("%" + content.getTitle() + "%");
            }
            if (content.getUrl() != null && content.getUrl().length() > 0) {
                criteria.andUrlLike("%" + content.getUrl() + "%");
            }
            if (content.getPic() != null && content.getPic().length() > 0) {
                criteria.andPicLike("%" + content.getPic() + "%");
            }
            if (content.getContent() != null && content.getContent().length() > 0) {
                criteria.andContentLike("%" + content.getContent() + "%");
            }
            if (content.getStatus() != null && content.getStatus().length() > 0) {
                criteria.andStatusLike("%" + content.getStatus() + "%");
            }

        }

        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<TbContent> findContentListByCategoryId(Long categoryId) {

        //1.从redis中查询数据  如果有 直接返回
       List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps("contentList").get(categoryId);

       if(contentList!=null && contentList.size()>0){
           System.out.println("===========有缓存=============");

           //有缓存
           return contentList;
       }

        //2如果redis中没有数据

        TbContentExample example = new TbContentExample();
        example.createCriteria().andStatusEqualTo("1").andCategoryIdEqualTo(categoryId);
        //select * from tb_content where category_id =1 and status='1'


        //3 查询mysql的数据列表存入到redis中  缓存的是List
        List<TbContent> contents = contentMapper.selectByExample(example);

        redisTemplate.boundHashOps("contentList").put(categoryId,contents);
        System.out.println("===============没有缓存==============");
        //redisTemplate.boundValueOps(categoryId,contents);
        return contents;




    }


}
