package com.pinyougou.seckill.pojo;

import java.io.Serializable;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.seckill.pojo
 * @since 1.0
 */
public class RecordInfo implements Serializable {

    private String userId;//下订单的用户的ID
    private Long id;//商品的ID

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
