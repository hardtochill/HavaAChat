package cn.havaachat.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 封装分页请求返回结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResultVO {
    /**
     * 当前页号
     */
    private Integer pageNo;
    /**
     * 一页记录数
     */
    private Integer pageSize;
    /**
     * 当前页记录数
     */
    private Long pageTotal;
    private List list;

}
