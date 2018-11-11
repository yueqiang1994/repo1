package com.pinyougou.pay.service;

import java.util.Map;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.pay.service
 * @since 1.0
 */
public interface WxPayService {

    //调用统一下单的API 生成支付二维码的连接地址 返回给Controller

    /**
     *
     * @param out_trade_no  品优购生成的支付订单号
     * @param total_fee     支付的金额
     * @return
     */
    public Map createNative(String out_trade_no,String total_fee);

    /**
     * 根据商户生成的支付的订单号来查询该支付订单的状态（成功，失败等）
     * @param out_trade_no
     * @return
     */
    public Map queryStatus(String out_trade_no);
}
