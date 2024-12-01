package cn.havaachat.service.impl;

import cn.havaachat.config.AppConfiguration;
import cn.havaachat.context.BaseContext;
import cn.havaachat.enums.*;
import cn.havaachat.exception.BaseException;
import cn.havaachat.mapper.*;
import cn.havaachat.pojo.dto.*;
import cn.havaachat.pojo.entity.*;
import cn.havaachat.pojo.vo.GroupInfoVO;
import cn.havaachat.pojo.vo.PageResultVO;
import cn.havaachat.redis.RedisService;
import cn.havaachat.service.ChatSessionUserService;
import cn.havaachat.service.GroupInfoService;
import cn.havaachat.service.UserContactService;
import cn.havaachat.utils.FilePathUtils;
import cn.havaachat.utils.StringUtils;
import cn.havaachat.websocket.ChannelContextUtils;
import cn.havaachat.websocket.MessageHandler;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jodd.util.ArraysUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class GroupInfoServiceImpl implements GroupInfoService {
    private GroupInfoMapper groupInfoMapper;
    private RedisService redisService;
    private UserContactMapper userContactMapper;
    private AppConfiguration appConfiguration;
    private ChatSessionMapper chatSessionMapper;
    private ChatSessionUserMapper chatSessionUserMapper;
    private ChatMessageMapper chatMessageMapper;
    private ChannelContextUtils channelContextUtils;
    private MessageHandler messageHandler;
    private ChatSessionUserService chatSessionUserService;
    private UserContactService userContactService;
    private  UserInfoMapper userInfoMapper;
    private GroupInfoService groupInfoService;
    @Autowired
    public GroupInfoServiceImpl(GroupInfoMapper groupInfoMapper, RedisService redisService, UserContactMapper userContactMapper,
                                AppConfiguration appConfiguration, ChatSessionMapper chatSessionMapper, ChatSessionUserMapper chatSessionUserMapper,
                                ChannelContextUtils channelContextUtils, ChatMessageMapper chatMessageMapper, MessageHandler messageHandler,
                                ChatSessionUserService chatSessionUserService, UserContactService userContactService, UserInfoMapper userInfoMapper,
                                @Lazy GroupInfoService groupInfoService){
        this.groupInfoMapper = groupInfoMapper;
        this.redisService = redisService;
        this.userContactMapper = userContactMapper;
        this.appConfiguration = appConfiguration;
        this.chatSessionMapper = chatSessionMapper;
        this.chatSessionUserMapper = chatSessionUserMapper;
        this.channelContextUtils = channelContextUtils;
        this.chatMessageMapper = chatMessageMapper;
        this.messageHandler = messageHandler;
        this.chatSessionUserService = chatSessionUserService;
        this.userContactService = userContactService;
        this.userInfoMapper = userInfoMapper;
        this.groupInfoService=groupInfoService;
    }
    /**
     * 新增或修改群组
     * @param saveGroupDTO
     */
    @Transactional
    public void saveGroupInfo(SaveGroupDTO saveGroupDTO) throws IOException {
        // 取出存入线程上下文的用户Token
        TokenUserInfoDTO tokenUserInfoDTO = BaseContext.getTokenUserInfo();
        GroupInfo groupInfo = new GroupInfo();
        BeanUtils.copyProperties(saveGroupDTO,groupInfo);
        groupInfo.setGroupOwnerId(tokenUserInfoDTO.getUserId());
        Long now = System.currentTimeMillis();
        // 新增群组
        if(StringUtils.isEmpty(groupInfo.getGroupId())){
            log.info("新增群组：{}",saveGroupDTO);
            // 判断创建者是否已达单人创建最大群组数
            Integer groupCount = groupInfoMapper.countByGroupOwnerId(groupInfo.getGroupOwnerId());
            SysSettingDTO sysSetting = redisService.getSysSetting();
            if(groupCount>=sysSetting.getMaxGroupCount()){
                throw new BaseException(ResponseCodeEnum.CODE_400.getCode(),"最多只能创建"+sysSetting.getMaxGroupCount()+"个群聊");
            }
            // 校验群头像是否为空
            if(null == saveGroupDTO.getAvatarFile()){
                throw new BaseException(ResponseCodeEnum.CODE_600);
            }
            // 生成群组id
            groupInfo.setGroupId(StringUtils.generateRandomGroupId());
            groupInfo.setStatus(GroupStatusEnum.NORMAL.getStatus());
            groupInfoMapper.insert(groupInfo);
            // 把新创建的群组加入当前用户的联系人中
            UserContact userContact = new UserContact();
            userContact.setUserId(groupInfo.getGroupOwnerId());
            userContact.setContactId(groupInfo.getGroupId());
            userContact.setContactType(UserContactTypeEnum.GROUP.getType());
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            userContactMapper.insert(userContact);

            // 创建ChatSession
            ChatSession chatSession = new ChatSession();
            String sessionId = StringUtils.getChatSessionIdForGroup(groupInfo.getGroupId());
            chatSession.setSessionId(sessionId);
            chatSession.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatSession.setLastReceiveTime(now);
            chatSessionMapper.insert(chatSession);
            // 为群主创建ChatSessionUser
            ChatSessionUser chatSessionUser = new ChatSessionUser();
            chatSessionUser.setUserId(groupInfo.getGroupOwnerId());
            chatSessionUser.setContactId(groupInfo.getGroupId());
            chatSessionUser.setSessionId(sessionId);
            chatSessionUser.setContactName(groupInfo.getGroupName());
            chatSessionUserMapper.insert(chatSessionUser);
            // 创建ChatMessage
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageType(MessageTypeEnum.GROUP_CREATE.getType());
            chatMessage.setMessageContent(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatMessage.setSendTime(now);
            chatMessage.setContactId(groupInfo.getGroupId());
            chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
            chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
            chatMessageMapper.insert(chatMessage);
            // 将群聊加入群主的联系人缓存
            redisService.saveUserContactId(groupInfo.getGroupOwnerId(), groupInfo.getGroupId());
            // 将群主加入该群聊的Channel列表
            channelContextUtils.addUser2Group(groupInfo.getGroupOwnerId(), groupInfo.getGroupId());
            // 发送ws消息
            SendMessageToFrontDTO sendMessageToFrontDTO = new SendMessageToFrontDTO();
            BeanUtils.copyProperties(chatMessage, sendMessageToFrontDTO);
            sendMessageToFrontDTO.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatSessionUser.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatSessionUser.setLastReceiveTime(now);
            // 刚创建群聊时，群员只有群主自己
            chatSessionUser.setMemberCount(1);
            sendMessageToFrontDTO.setExtendData(chatSessionUser);
            messageHandler.sendMessage(sendMessageToFrontDTO);
        }else{ // 修改群组
            log.info("修改群组：{}",saveGroupDTO);
            GroupInfo originGroupInfo = groupInfoMapper.findById(groupInfo.getGroupId());
            // 当前申请修改群信息的用户 不是 该群的群主
            if(!groupInfo.getGroupOwnerId().equals(originGroupInfo.getGroupOwnerId())){
                throw new BaseException(ResponseCodeEnum.CODE_600);
            }
            groupInfoMapper.update(groupInfo);

            // 判断本次修改是否涉及到名称的修改，若涉及名称修改，则还要更新会话中的昵称信息
            if (!originGroupInfo.getGroupName().equals(groupInfo.getGroupName())){
                // 更新所有群员会话中的昵称信息
                chatSessionUserService.updateChatSessionUserName(groupInfo.getGroupId(), groupInfo.getGroupName());
            }
        }
        if(null == saveGroupDTO.getAvatarFile()){
            return;
        }
        // 将头像文件存入本地
        String avatarFileFolderPath = FilePathUtils.generateAvatarFileFolderPath(appConfiguration.getFileFolder());
        // 创建头像文件所在目录
        File avatarFileFolder = new File(avatarFileFolderPath);
        if(!avatarFileFolder.exists()){
            avatarFileFolder.mkdirs();
        }
        // 创建头像文件所在路径
        String avatarFilePath = FilePathUtils.generateAvatarFilePath(avatarFileFolderPath,groupInfo.getGroupId());
        String coverAvatarFilePath = FilePathUtils.generateCoverAvatarFilePath(avatarFileFolderPath,groupInfo.getGroupId());
        // 存入本地
        saveGroupDTO.getAvatarFile().transferTo(new File(avatarFilePath));
        saveGroupDTO.getAvatarCover().transferTo(new File(coverAvatarFilePath));
    }

    /**
     * 获取用户创建的群聊
     * @param userId
     * @return
     */
    @Override
    public List<GroupInfo> loadMyGroup(String userId) {
        return groupInfoMapper.findBatchByGroupOwnerIdAndStatus(userId,GroupStatusEnum.NORMAL.getStatus());
    }

    /**
     * 获取群聊详细信息
     * @param groupId
     * @return
     */
    @Override
    public GroupInfo getGroupInfo(String groupId) {
        log.info("查询群聊详细信息：{}",groupId);
        // 校验并获取群聊信息
        GroupInfo groupInfo = checkAndGetGroupInfoByGroupId(groupId);
        // 查询群成员数量
        Integer memberCount = userContactMapper.countByContactIdAndStatus(groupId,UserContactStatusEnum.FRIEND.getStatus());
        groupInfo.setMemberCount(memberCount);
        return groupInfo;
    }

    /**
     * 在聊天会话界面获取群聊详细信息
     * @param groupId
     * @return
     */
    @Override
    public GroupInfoVO getGroupInfo4Chat(String groupId) {
        log.info("在聊天会话界面查询群聊详细信息：{}",groupId);
        // 校验并获取群聊信息
        GroupInfo groupInfo = checkAndGetGroupInfoByGroupId(groupId);
        // 获取该群聊所有有效联系人
        List<UserContact> userContactList = userContactMapper.findBatchWithContactNameAndSexByContactIdAndStatus(groupId, UserContactStatusEnum.FRIEND.getStatus());
        GroupInfoVO groupInfoVO = new GroupInfoVO();
        groupInfoVO.setGroupInfo(groupInfo);
        groupInfoVO.setUserContactList(userContactList);
        return groupInfoVO;
    }
    /**
     * 获取群组列表
     * @param pageDTO
     * @param groupId
     * @param groupNameFuzzy
     * @param groupOwnerId
     * @return
     */
    @Override
    public PageResultVO loadGroup(PageDTO pageDTO, String groupId, String groupNameFuzzy, String groupOwnerId) {
        log.info("管理后台：分页查询群组列表：{}",pageDTO);
        PageHelper.startPage(pageDTO.getPageNo(),pageDTO.getPageSize());
        GroupInfo groupInfoForQuery = new GroupInfo();
        groupInfoForQuery.setGroupId(StringUtils.isEmpty(groupId)?null:groupId);
        groupInfoForQuery.setGroupName(StringUtils.isEmpty(groupNameFuzzy)?null:groupNameFuzzy);
        groupInfoForQuery.setGroupOwnerId(StringUtils.isEmpty(groupOwnerId)?null:groupOwnerId);
        Page<GroupInfo> page = groupInfoMapper.findBatch(groupInfoForQuery);
        return new PageResultVO(pageDTO.getPageNo(), pageDTO.getPageSize(), page.getTotal(),page.getResult());
    }

    /**
     * 解散群组
     * @param groupId
     */
    @Override
    @Transactional
    public void dissolutionGroup(String groupId) {
        log.info("管理后台：解散群组：groupId：{}",groupId);
        GroupInfo existGroupInfo = groupInfoMapper.findById(groupId);
        if(null==existGroupInfo){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        // 更新群组状态
        GroupInfo groupInfoForUpdate = new GroupInfo();
        groupInfoForUpdate.setGroupId(groupId);
        groupInfoForUpdate.setStatus(GroupStatusEnum.DISSOLUTION.getStatus());
        groupInfoMapper.update(groupInfoForUpdate);
        // 更新与群组相关的所有联系
        UserContact userContactForUpdate = new UserContact();
        userContactForUpdate.setContactId(groupId);
        userContactForUpdate.setStatus(UserContactStatusEnum.DEL.getStatus());
        userContactMapper.updateStatusByContactId(userContactForUpdate);

        // 移除相关群员的联系人缓存
        // 取出该群所有群员
        List<UserContact> grouperContactList = userContactMapper.findBatchByContactId(groupId);
        // 移除群员的联系人缓存中的群聊信息
        for (UserContact grouperContact : grouperContactList) {
            redisService.removeUserContactId(grouperContact.getUserId(),groupId);
        }

        // 更新ChatSession
        String sessionId = StringUtils.getChatSessionIdForGroup(groupId);
        String messageContent = MessageTypeEnum.DISSOLUTION_GROUP.getInitMessage();
        Long now = System.currentTimeMillis();
        ChatSession chatSession = chatSessionMapper.findBySessionId(sessionId);
        chatSession.setLastMessage(messageContent);
        chatSession.setLastReceiveTime(now);
        chatSessionMapper.update(chatSession);

        // 保存ChatMessage
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageType(MessageTypeEnum.DISSOLUTION_GROUP.getType());
        chatMessage.setMessageContent(messageContent);
        chatMessage.setSendTime(now);
        chatMessage.setContactId(groupId);
        chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
        chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
        chatMessageMapper.insert(chatMessage);

        // 发送ws消息
        SendMessageToFrontDTO sendMessageToFrontDTO = new SendMessageToFrontDTO();
        BeanUtils.copyProperties(chatMessage,sendMessageToFrontDTO);
        messageHandler.sendMessage(sendMessageToFrontDTO);
    }

    /**
     * 校验用户与群聊的联系关系并获取该群聊信息
     * @param groupId
     * @return
     */
    public GroupInfo checkAndGetGroupInfoByGroupId(String groupId){
        // 取出当前用户信息
        TokenUserInfoDTO tokenUserInfo = BaseContext.getTokenUserInfo();
        UserContact userContact = userContactMapper.findByUserIdAndContactId(tokenUserInfo.getUserId(), groupId);
        // 校验当前用户是否在要查询的群聊中 和 该联系关系是否已删除或拉黑
        if(null==userContact || !userContact.getStatus().equals(UserContactStatusEnum.FRIEND.getStatus())){
            throw new BaseException(ResponseCodeEnum.CODE_400.getCode(),"你不在群聊中或群聊不存在或群聊已解散");
        }
        // 校验该群聊是否还存在
        GroupInfo groupInfo = groupInfoMapper.findById(groupId);
        if(null==groupInfo || !GroupStatusEnum.NORMAL.getStatus().equals(groupInfo.getStatus())){
            throw new BaseException(ResponseCodeEnum.CODE_400.getCode(),"群聊不存在或已解散");
        }
        return groupInfo;
    }
    /**
     * 退出群聊（主动或被动）
     * @param userId
     * @param groupId
     * @param messageTypeEnum
     */
    @Override
    @Transactional
    public void leaveGroup(String userId, String groupId, MessageTypeEnum messageTypeEnum){
        log.info("退出群聊：userId={}，groupId={}，groupLeaveType={}",userId,groupId,messageTypeEnum);
        if(messageTypeEnum!=MessageTypeEnum.LEAVE_GROUP && messageTypeEnum!=MessageTypeEnum.REMOVE_GROUP){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        GroupInfo groupInfo = groupInfoMapper.findById(groupId);
        if (null==groupInfo || groupInfo.getGroupOwnerId().equals(userId)){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        // 删除群员
        Integer count = userContactMapper.deleteByUserIdAndContactId(userId, groupId);
        if (0==count){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        UserInfo userInfo = userInfoMapper.findById(userId);
        String sessionId = StringUtils.getChatSessionIdForGroup(groupId);
        Long now = System.currentTimeMillis();
        String messageContent = String.format(messageTypeEnum.getInitMessage(),userInfo.getNickName());

        // 更新ChatSession
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        chatSession.setLastMessage(messageContent);
        chatSession.setLastReceiveTime(now);
        chatSessionMapper.update(chatSession);

        // 保存ChatMessage
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageType(messageTypeEnum.getType());
        chatMessage.setMessageContent(messageContent);
        chatMessage.setSendTime(now);
        chatMessage.setContactId(groupId);
        chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
        chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
        chatMessageMapper.insert(chatMessage);

        // 发送ws消息
        Integer memberCount = userContactMapper.countByContactIdAndStatus(groupId, UserContactStatusEnum.FRIEND.getStatus());
        SendMessageToFrontDTO sendMessageToFrontDTO = new SendMessageToFrontDTO();
        BeanUtils.copyProperties(chatMessage,sendMessageToFrontDTO);
        sendMessageToFrontDTO.setExtendData(userId);
        sendMessageToFrontDTO.setMemberCount(memberCount);
        messageHandler.sendMessage(sendMessageToFrontDTO);

        // 移除redis缓存
        redisService.removeUserContactId(userId,groupId);
    }
    /**
     * 添加或移除群成员
     * @param addOrRemoveGroupUserDTO
     */
    @Override
    public void addOrRemoveGroupUser(AddOrRemoveGroupUserDTO addOrRemoveGroupUserDTO) {
        log.info("添加或移除群成员：{}",addOrRemoveGroupUserDTO);
        String groupId = addOrRemoveGroupUserDTO.getGroupId();
        String selectContacts = addOrRemoveGroupUserDTO.getSelectContacts();
        Integer opType = addOrRemoveGroupUserDTO.getOpType();
        String userId = BaseContext.getTokenUserInfo().getUserId();
        GroupInfo groupInfo = groupInfoMapper.findById(groupId);

        // 校验群聊是否存在 和 该操作用户是否为群主
        if (null==groupInfo || !groupInfo.getGroupOwnerId().equals(userId)){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        // 校验该操作是否存在
        if (!ArraysUtil.contains(new int[]{GroupOperationTypeEnum.REMOVE.getType(),GroupOperationTypeEnum.ADD.getType()},opType)){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }

        // 要操作的用户id
        String[] contactIdList = selectContacts.split(",");
        for (String contactId : contactIdList) {
            if (GroupOperationTypeEnum.REMOVE.getType().equals(opType)){ // 移除
                // 自己调用自己以保证事务生效
                groupInfoService.leaveGroup(contactId,groupId,MessageTypeEnum.REMOVE_GROUP);
            }else{ // 添加
                userContactService.addContact(contactId,groupInfo.getGroupOwnerId(),groupId,UserContactTypeEnum.GROUP.getType(), null);
            }
        }
    }
}
