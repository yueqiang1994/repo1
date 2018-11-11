package com.pinyougou;

import static org.junit.Assert.assertTrue;

import com.pinyougou.pojo.TbItem;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * Unit test for simple App.
 */
@ContextConfiguration("classpath:spring/applicationContext-solr.xml")
@RunWith(SpringRunner.class)
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()  throws Exception{
        //1.用solrj来实现
        //从数据库中查询数据
        //1.创建一个连接对象
        SolrServer solrServer = new HttpSolrServer("http://192.168.25.154:8080/solr/collection1");
        //2.创建文档
        SolrInputDocument document = new SolrInputDocument();
        //3.添加域
        document.addField("id", "test001");//
        document.addField("item_title", "测试");
        //4.添加文档到索引库中
        solrServer.add(document);
        solrServer.commit();
        System.out.println("Hello World!");
    }
    //spring data solr

    @Autowired
    private SolrTemplate solrTemplate;

    //添加文档
   @Test
   public void add(){
       TbItem item = new TbItem();//item 就相当于一个SolrInputDocument
       item.setId(1L);
       item.setTitle("测试商品");//solrj的注解@Field
       solrTemplate.saveBean(item);
       solrTemplate.commit();
   }

    @Test
    public void addFor(){

        for (long i = 0; i < 100; i++) {
            TbItem item = new TbItem();//item 就相当于一个SolrInputDocument
            item.setId(i);
            item.setTitle("测试商品"+i);//solrj的注解@Field
            solrTemplate.saveBean(item);
            solrTemplate.commit();
        }

    }

   //查询
    //根据ID查询
    @Test
    public void find(){
       //要注意，注解不能少
        TbItem tbItem = solrTemplate.getById("1", TbItem.class);
        System.out.println(tbItem.getTitle()+tbItem.getId());
    }

    @Test
    public void delete(){
        Query query = new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    //根据查询的条件来查询   分页查询
    @Test
    public void findByCriteriaAndPage(){
        Query query = new SimpleQuery("*:*");//SolrQuery query = new SolrQuery("*:*");


        //设置各种条件  item_title:手机
        Criteria criteria = new Criteria("item_title");
        criteria.is("9");
//        criteria.contains("9");
        query.addCriteria(criteria);

        //设置分页
        query.setOffset(0);//设置开始分页的位置（page-1）* rows
        query.setRows(20);//每页显示的行

        //分页查询
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);


        //获取记录
        System.out.println("总记录数"+page.getTotalElements());

        List<TbItem> content = page.getContent();//查询当前页的记录
        for (TbItem tbItem : content) {
            System.out.println(tbItem.getTitle());
        }

        System.out.println("总页数"+page.getTotalPages());


    }




}
