package cn.havaachat.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 分页查询
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO {
    private Integer pageNo;

    private Integer pageSize;
}
