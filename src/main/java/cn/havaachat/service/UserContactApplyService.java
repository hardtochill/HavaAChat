package cn.havaachat.service;

import cn.havaachat.pojo.dto.ContactApplyAddDTO;
import cn.havaachat.pojo.dto.PageDTO;
import cn.havaachat.pojo.vo.PageResultVO;

/**
 * 联系人申请
 */
public interface UserContactApplyService {
    /**
     * 申请添加联系人
     * @param contactApplyAddDTO
     * @return 返回添加类型
     */
    Integer applyAdd(ContactApplyAddDTO contactApplyAddDTO);

    /**
     * 分页获取申请列表
     * @param pageDTO
     * @return
     */
    PageResultVO loadApply(PageDTO pageDTO);

    /**
     * 处理好友申请
     * @param applyId
     * @param status
     */
    void dealWithApply(Integer applyId,Integer status);
}
