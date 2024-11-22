package cn.havaachat.service.impl;

import cn.havaachat.context.BaseContext;
import cn.havaachat.enums.*;
import cn.havaachat.exception.BaseException;
import cn.havaachat.mapper.*;
import cn.havaachat.pojo.dto.SysSettingDTO;
import cn.havaachat.pojo.entity.*;
import cn.havaachat.pojo.vo.UserContactLoadResultVO;
import cn.havaachat.pojo.vo.UserContactSearchResultVO;
import cn.havaachat.pojo.vo.UserInfoVO;
import cn.havaachat.redis.RedisService;
import cn.havaachat.service.UserContactService;
import cn.havaachat.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserContactServiceImpl implements UserContactService {
    private UserContactMapper userContactMapper;
    private GroupInfoMapper groupInfoMapper;
    private UserInfoMapper userInfoMapper;
    private RedisService redisService;
    private ChatSessionMapper chatSessionMapper;
    private ChatSessionUserMapper chatSessionUserMapper;
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    public UserContactServiceImpl(UserContactMapper userContactMapper,UserInfoMapper userInfoMapper,GroupInfoMapper groupInfoMapper,
                                  RedisService redisService,ChatSessionMapper chatSessionMapper,ChatSessionUserMapper chatSessionUserMapper,
                                  ChatMessageMapper chatMessageMapper){
        this.userContactMapper = userContactMapper;
        this.userInfoMapper = userInfoMapper;
        this.groupInfoMapper = groupInfoMapper;
        this.redisService = redisService;
        this.chatSessionMapper = chatSessionMapper;
        this.chatSessionUserMapper = chatSessionUserMapper;
        this.chatMessageMapper = chatMessageMapper;
    }
    /**
     * 搜索联系人
     * @param contactId
     * @return
     */
    @Override
    public UserContactSearchResultVO search(String contactId) {
        log.info("搜索联系人：{}",contactId);
        // 判断要搜索的联系人是用户还是群聊
        UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getById(contactId);
        if(null==userContactTypeEnum){
            return null;
        }
        // 取出当前用户信息
        String userId = BaseContext.getTokenUserInfo().getUserId();
        UserContactSearchResultVO userContactSearchResultVO = new UserContactSearchResultVO();
        switch (userContactTypeEnum){
            case USER:
                UserInfo contactUser = userInfoMapper.findById(contactId);
                userContactSearchResultVO.setNickName(contactUser.getNickName());
                userContactSearchResultVO.setSex(contactUser.getSex());
                userContactSearchResultVO.setAreaName(contactUser.getAreaName());
                break;
            case GROUP:
                GroupInfo contactGroup = groupInfoMapper.findById(contactId);
                userContactSearchResultVO.setNickName(contactGroup.getGroupName());
                break;
        }
        userContactSearchResultVO.setContactType(userContactTypeEnum.toString());
        userContactSearchResultVO.setContactId(contactId);
        // 自己搜自己则直接返回
        if(userId.equals(contactId)){
            userContactSearchResultVO.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            return userContactSearchResultVO;
        }
        UserContact searchResultUserContact = userContactMapper.findByUserIdAndContactId(userId,contactId);
        userContactSearchResultVO.setStatus(searchResultUserContact==null?null:searchResultUserContact.getStatus());
        return userContactSearchResultVO;
    }
    /**
     * 添加联系人
     * @param applyUserId
     * @param receiveUserId
     * @param contactId
     * @param contactType
     */
    @Override
    public void addContact(String applyUserId,String receiveUserId,String contactId,Integer contactType){
        log.info("添加联系人：applyUserId：{}，receiveUserId：{}，contactId：{}，contactType：{}",applyUserId,receiveUserId,contactId,contactType);
        // 若是群聊添加联系人，需判断上限
        if(UserContactTypeEnum.GROUP.getType().equals(contactType)){
            Integer groupNum = userContactMapper.countByContactIdAndStatus(contactId,UserContactStatusEnum.FRIEND.getStatus());
            SysSettingDTO sysSetting = redisService.getSysSetting();
            if(groupNum>=sysSetting.getMaxGroupCount()){
                throw new BaseException(ResponseCodeEnum.CODE_400.getCode(),"群成员已满，无法加入");
            }
        }
        List<UserContact> userContactList = new ArrayList<>();
        // 被申请人同意申请后，申请人添加被申请人为好友
        UserContact applyUserContact = new UserContact();
        applyUserContact.setUserId(applyUserId);
        applyUserContact.setContactId(contactId);
        applyUserContact.setContactType(contactType);
        applyUserContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        userContactList.add(applyUserContact);
        // 如果被申请人是好友，则被申请人还需记录申请人为好友；如果被申请人是群聊，则群聊无需记录申请人为好友
        if(UserContactTypeEnum.USER.getType().equals(contactType)){
            UserContact receiveUserContact = new UserContact();
            receiveUserContact.setUserId(receiveUserId);
            receiveUserContact.setContactId(applyUserId);
            receiveUserContact.setContactType(contactType);
            receiveUserContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            userContactList.add(receiveUserContact);
        }
        // 在申请添加部分对拉黑状态做了拦截，走到这的逻辑只有：非好友 or 已删除后再添加
        UserContact existContact = userContactMapper.findByUserIdAndContactId(applyUserId, receiveUserId);
        if(null==existContact){// 非好友
            userContactMapper.insertBatch(userContactList);
        }else{// 已删除后再添加
            userContactMapper.updateBatch(userContactList);
        }
        // todo 将联系人添加缓存
        // todo 创建会话 发送消息
    }

    /**
     * 为新创建用户添加机器人好友
     * @param userId
     */
    @Transactional
    @Override
    public void addContact4Robot(String userId) {
        log.info("为新注册用户添加机器人好友：userId={}",userId);
        SysSettingDTO sysSettingDTO = redisService.getSysSetting();
        String contactId = sysSettingDTO.getRobotUid();
        String contactName = sysSettingDTO.getRobotNickName();
        String sendMessage = sysSettingDTO.getRobotWelcome();
        StringUtils.cleanHtmlTag(sendMessage);
        // 添加机器人好友
        UserContact userContactForInsert = new UserContact();
        userContactForInsert.setUserId(userId);
        userContactForInsert.setContactId(contactId);
        userContactForInsert.setContactType(UserContactTypeEnum.USER.getType());
        userContactForInsert.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        userContactMapper.insert(userContactForInsert);
        // 机器人会向用户发送欢迎语句，因此要新增会话信息
        Long messageTime = System.currentTimeMillis();
        // 增加会话信息
        String sessionId = StringUtils.getChatSessionId(new String[]{userId,contactId});
        ChatSession chatSessionForInsert = new ChatSession();
        chatSessionForInsert.setSessionId(sessionId);
        chatSessionForInsert.setLastMessage(sendMessage);
        chatSessionForInsert.setLastReceiveTime(messageTime);
        chatSessionMapper.insert(chatSessionForInsert);
        // 增加会话用户信息
        ChatSessionUser chatSessionUserForInsert = new ChatSessionUser();
        chatSessionUserForInsert.setUserId(userId);
        chatSessionUserForInsert.setContactId(contactId);
        chatSessionUserForInsert.setSessionId(sessionId);
        chatSessionUserForInsert.setContactName(contactName);
        chatSessionUserMapper.insert(chatSessionUserForInsert);
        // 增加聊天信息
        ChatMessage chatMessageForInsert = new ChatMessage();
        chatMessageForInsert.setSessionId(sessionId);
        chatMessageForInsert.setMessageType(MessageTypeEnum.CHAT.getType());
        chatMessageForInsert.setMessageContent(sendMessage);
        // 消息发送方是机器人
        chatMessageForInsert.setSendUserId(contactId);
        chatMessageForInsert.setSendUserNickName(contactName);
        chatMessageForInsert.setSendTime(messageTime);
        // 消息接收方是新注册的用户
        chatMessageForInsert.setContactId(userId);
        chatMessageForInsert.setContactType(UserContactTypeEnum.USER.getType());
        chatMessageForInsert.setStatus(MessageStatusEnum.SENDED.getStatus());
        chatMessageMapper.insert(chatMessageForInsert);
    }

    /**
     * 获取联系人列表
     * @param contactType
     * @return
     */
    @Override
    public List<UserContactLoadResultVO> loadContact(String contactType) {
        String userId = BaseContext.getTokenUserInfo().getUserId();
        log.info("获取联系人列表：userId：{}，contactType：{}",userId,contactType);
        // 要查询的联系人类型
        UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getByName(contactType);
        if(null==userContactTypeEnum){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        // 可被查到的联系状态：好友、被删除、被拉黑
        List<Integer> statusList = new ArrayList<>();
        statusList.add(UserContactStatusEnum.FRIEND.getStatus());
        statusList.add(UserContactStatusEnum.DEL_BE.getStatus());
        statusList.add(UserContactStatusEnum.BLACKLIST_BE.getStatus());
        // 查群聊或好友
        List<UserContactLoadResultVO> userContactLoadResultVOList = userContactMapper.findBatchWithContactNameByUserIdAndContactTypeAndStatusList(userId, userContactTypeEnum.getType(), statusList);
        return userContactLoadResultVOList;
    }
    /**
     * 获取目标用户信息（从好友列表或从群成员），目标用户可以是好友也可以是非好友
     * @param targetUserId
     * @return
     */
    @Override
    public UserInfoVO getContactInfoForAll(String targetUserId) {
        String userId = BaseContext.getTokenUserInfo().getUserId();
        log.info("获取目标用户信息，userId：{}，targetUserId：{}",userId,targetUserId);
        UserInfo targetUserInfo = userInfoMapper.findById(targetUserId);
        UserInfoVO targetUserInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(targetUserInfo,targetUserInfoVO);
        targetUserInfoVO.setContactStatus(UserContactStatusEnum.NOT_FRIEND.getStatus());
        // 判断用户是否与目标用户存在好友关系
        UserContact existContact = userContactMapper.findByUserIdAndContactId(userId, targetUserId);
        if(null!=existContact){
            // todo 这部分的前端就两种处理方式，如果是FRIEND就显示“发送消息”，其它都显示“加为好友”，看后续“发送消息”是否做校验，如果不做校验，此处还要额外处理 拉黑和删除
            targetUserInfoVO.setContactStatus(UserContactStatusEnum.FRIEND.getStatus());
        }
        return targetUserInfoVO;
    }
    /**
     * 获取目标用户信息（从好友列表），目标用户只能是好友
     * @param targetUserId
     * @return
     */
    @Override
    public UserInfoVO getContactInfoOnlyFriend(String targetUserId) {
        String userId = BaseContext.getTokenUserInfo().getUserId();
        log.info("获取目标好友用户信息，userId：{}，targetUserId：{}",userId,targetUserId);
        UserContact existContact = userContactMapper.findByUserIdAndContactId(userId, targetUserId);
        // 联系无效
        if(null==existContact || !ArrayUtils.contains(new Integer[]{
                UserContactStatusEnum.FRIEND.getStatus(),UserContactStatusEnum.DEL_BE.getStatus(),UserContactStatusEnum.BLACKLIST_BE.getStatus()
        },existContact.getStatus())){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        UserInfoVO targetUserInfoVO = new UserInfoVO();
        UserInfo targetUserInfo = userInfoMapper.findById(targetUserId);
        BeanUtils.copyProperties(targetUserInfo,targetUserInfoVO);
        return targetUserInfoVO;
    }
    /**
     * 删除或拉黑联系人
     * @param contactId
     * @param userContactStatusEnum
     */
    @Override
    @Transactional
    public void addContactToDeleteOrBlackList(String contactId, UserContactStatusEnum userContactStatusEnum) {
        String userId = BaseContext.getTokenUserInfo().getUserId();
        log.info("删除或拉黑联系人：userId：{}，contactId：{}，status：{}",userId,contactId,userContactStatusEnum.getDescription());
        // 修改 当前用户——>被删除或被拉黑用户 的联系状态
        List<UserContact> userContactList = new ArrayList<>();
        UserContact userContact = new UserContact();
        userContact.setUserId(userId);
        userContact.setContactId(contactId);
        userContact.setStatus(userContactStatusEnum.getStatus());
        userContactList.add(userContact);
        // 修改 被删除或拉黑用户——>当前用户 的联系状态
        UserContact beUserContact = new UserContact();
        beUserContact.setUserId(contactId);
        beUserContact.setContactId(userId);
        if(UserContactStatusEnum.DEL.equals(userContactStatusEnum)){
            beUserContact.setStatus(UserContactStatusEnum.DEL_BE.getStatus());
        }else if(UserContactStatusEnum.BLACKLIST.equals(userContactStatusEnum)){
            beUserContact.setStatus(UserContactStatusEnum.BLACKLIST_BE.getStatus());
        }
        userContactList.add(beUserContact);
        userContactMapper.updateBatch(userContactList);
    }

}
