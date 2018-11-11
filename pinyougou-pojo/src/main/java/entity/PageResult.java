package entity;

import com.pinyougou.pojo.TbBrand;

import java.io.Serializable;
import java.util.List;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package entity
 * @since 1.0
 */
public class PageResult implements Serializable {
    private List rows;
    private Long total;

    public PageResult() {

    }

    public PageResult(Long total,List rows) {
        this.rows = rows;
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
