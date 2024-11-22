package cn.havaachat.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 联系人列表VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserContactLoadResultVO {
    /**
     * 用户id
     */
    private String userId;
    /**
     * 联系人id或群组id
     */
    private String contactId;
    /**
     * 联系人类型 0：好友，1：群组
     */
    private Integer contactType;
    /**
     * 状态：0.非好友，1.好友，2.已删除好友，3.被好友删除，4.已拉黑好友，5.被好友拉黑
     */
    private Integer status;
    /**
     * 联系人名称
     */
    private String contactName;
}
