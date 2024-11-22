package cn.havaachat.pojo.vo;

import cn.havaachat.enums.UserContactStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索好友结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserContactSearchResultVO {
    /**
     * 搜索到的联系人或群组id
     */
    private String contactId;
    /**
     * 联系人类型，好友 or 群组
     */
    private String contactType;
    /**
     * 联系人名称
     */
    private String nickName;
    /**
     * 联系人状态：0.非好友，1.好友，2.已删除好友，3.被好友删除，4.已拉黑好友，5.被好友拉黑
     */
    private Integer status;
    /**
     * 状态描述
     */
    private String statusName;
    /**
     * 联系人性别
     */
    private Integer sex;
    /**
     * 联系人所在区域
     */
    private String areaName;
    public String getStatusName(){
        UserContactStatusEnum userContactStatusEnum = UserContactStatusEnum.getByStatus(status);
        return userContactStatusEnum==null?null:userContactStatusEnum.getDescription();
    }
}

