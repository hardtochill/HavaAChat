package cn.havaachat.service;

import cn.havaachat.enums.UserContactStatusEnum;
import cn.havaachat.pojo.dto.ContactApplyAddDTO;
import cn.havaachat.pojo.vo.PageResultVO;
import cn.havaachat.pojo.vo.UserContactLoadResultVO;
import cn.havaachat.pojo.vo.UserContactSearchResultVO;
import cn.havaachat.pojo.vo.UserInfoVO;

import java.util.List;

public interface UserContactService {
    /**
     * 搜索联系人
     * @param contactId
     * @return
     */
    UserContactSearchResultVO search(String contactId);

    /**
     * 添加联系人
     * @param applyUserId
     * @param receiveUserId
     * @param contactId
     * @param contactType
     */
    void addContact(String applyUserId,String receiveUserId,String contactId,Integer contactType,String applyInfo);

    /**
     * 为新注册用户添加机器人好友
     * @param userId
     */
    void addContact4Robot(String userId);

    /**
     * 获取联系人列表
     * @param contactType
     * @return
     */
    List<UserContactLoadResultVO> loadContact(String contactType);
    /**
     * 获取目标用户信息（从好友列表或从群成员），目标用户可以是好友也可以是非好友
     * @param targetUserId
     * @return
     */
    UserInfoVO getContactInfoForAll(String targetUserId);
    /**
     * 获取目标用户信息（从好友列表），目标用户只能是好友
     * @param targetUserId
     * @return
     */
    UserInfoVO getContactInfoOnlyFriend(String targetUserId);

    /**
     * 删除或拉黑联系人
     * @param contactId
     * @param userContactStatusEnum
     */
    void addContactToDeleteOrBlackList(String contactId, UserContactStatusEnum userContactStatusEnum);
}
