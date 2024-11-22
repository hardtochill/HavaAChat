package cn.havaachat.pojo.vo;

import cn.havaachat.pojo.entity.GroupInfo;
import cn.havaachat.pojo.entity.UserContact;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 群聊详细信息VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupInfoVO {
    /**
     * 该群聊对象
     */
    private GroupInfo groupInfo;
    /**
     * 该群聊所有联系人关系
     */
    private List<UserContact> userContactList;
}
