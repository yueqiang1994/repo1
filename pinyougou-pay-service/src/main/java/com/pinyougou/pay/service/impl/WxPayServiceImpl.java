package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.common.util.HttpClient;
import com.pinyougou.pay.service.WxPayService;
import org.springframework.beans.factory.annotation.Value;


import java.util.HashMap;
import java.util.Map;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.pay.service.impl
 * @since 1.0
 */
@Service
public class WxPayServiceImpl implements WxPayService {



    @Value("${appid}")
    private String appid;//公众号ID

    @Value("${partner}")
    private String partner;//商户ID

    @Value("${partnerkey}")
    private String partnerkey;//秘钥



    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        try {
            Map<String,String> resultMap = new HashMap();
            //1.组装参数Map
            Map<String,String> paramMap = new HashMap<>();
            //。。。。。设置参数
            paramMap.put("appid",appid);
            paramMap.put("mch_id",partner);
            paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
            paramMap.put("body","品优购");
            paramMap.put("out_trade_no",out_trade_no);
            paramMap.put("total_fee",total_fee);
            paramMap.put("spbill_create_ip","127.0.0.1");//终端的IP
            paramMap.put("notify_url","http://a31ef7db.ngrok.io/WeChatPay/WeChatPayNotify");
            paramMap.put("trade_type","NATIVE");//支付的类型 扫码支付


            //签名 不写，下边的方法会自动添加签名的
            String xmlParam = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            //2.创建httpclient  String
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");//打开浏览器 输入接口的地址
            client.setHttps(true);//使用https的方式发送请求
            client.setXmlParam(xmlParam);//设置发送请求的时候所携带的参数值（xml）
            //3.调用下单的API 接口
            client.post();//浏览器中回车 发送请求了
            String result = client.getContent();//
            System.out.println(result);
            Map<String, String> map = WXPayUtil.xmlToMap(result);

            resultMap.put("out_trade_no",out_trade_no);
            resultMap.put("total_fee",total_fee);
            resultMap.put("code_url",map.get("code_url"));
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap();
    }

    @Override
    public Map queryStatus(String out_trade_no) {
        try {
            Map<String,String> resultMap = new HashMap();
            //1.组装参数Map
            Map<String,String> paramMap = new HashMap<>();
            //。。。。。设置参数
            paramMap.put("appid",appid);
            paramMap.put("mch_id",partner);
            paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
            paramMap.put("out_trade_no",out_trade_no);


            //签名 不写，下边的方法会[自动添加签名]的
            String xmlParam = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            //2.创建httpclient  String
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");//打开浏览器 输入接口的地址
            client.setHttps(true);//使用https的方式发送请求
            client.setXmlParam(xmlParam);//设置发送请求的时候所携带的参数值（xml）
            //3.调用下单的API 接口
            client.post();//浏览器中回车 发送请求了
            String result = client.getContent();//
            System.out.println(result);
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
