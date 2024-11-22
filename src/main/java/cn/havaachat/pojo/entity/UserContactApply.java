package cn.havaachat.pojo.entity;

import cn.havaachat.enums.UserContactApplyStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 联系人申请
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserContactApply extends BaseEntity{
    /**
     * 自增id
     */
    private Integer applyId;
    /**
     * 申请人id
     */
    private String applyUserId;
    /**
     * 被申请人id
     */
    private String receiveUserId;
    /**
     * 申请人名称
     */
    private String contactName;
    /**
     * 联系人id或群组id
     */
    private String contactId;
    /**
     * 联系人类型 0：好友，1：群组
     */
    private Integer contactType;
    /**
     * 状态 0：待处理，1：已同意，2：已拒绝，3：已拉黑
     */
    private Integer status;
    /**
     * 状态名称
     */
    private String statusName;
    /**
     * 申请信息
     */
    private String applyInfo;
    /**
     * 最后申请时间
     */
    private Long lastApplyTime;
    public String getStatusName(){
        UserContactApplyStatusEnum userContactApplyStatusEnum = UserContactApplyStatusEnum.getByStatus(status);
        return userContactApplyStatusEnum==null?null:userContactApplyStatusEnum.getDescription();
    }
}
