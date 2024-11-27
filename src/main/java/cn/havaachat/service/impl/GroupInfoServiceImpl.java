package cn.havaachat.service.impl;

import cn.havaachat.config.AppConfiguration;
import cn.havaachat.context.BaseContext;
import cn.havaachat.enums.*;
import cn.havaachat.exception.BaseException;
import cn.havaachat.mapper.*;
import cn.havaachat.pojo.dto.SendMessageToFrontDTO;
import cn.havaachat.pojo.dto.SaveGroupDTO;
import cn.havaachat.pojo.dto.SysSettingDTO;
import cn.havaachat.pojo.dto.TokenUserInfoDTO;
import cn.havaachat.pojo.entity.*;
import cn.havaachat.pojo.vo.GroupInfoVO;
import cn.havaachat.redis.RedisService;
import cn.havaachat.service.ChatSessionUserService;
import cn.havaachat.service.GroupInfoService;
import cn.havaachat.utils.FilePathUtils;
import cn.havaachat.utils.StringUtils;
import cn.havaachat.websocket.ChannelContextUtils;
import cn.havaachat.websocket.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    public GroupInfoServiceImpl(GroupInfoMapper groupInfoMapper,RedisService redisService,UserContactMapper userContactMapper,
                                AppConfiguration appConfiguration,ChatSessionMapper chatSessionMapper,ChatSessionUserMapper chatSessionUserMapper,
                                ChannelContextUtils channelContextUtils,ChatMessageMapper chatMessageMapper,MessageHandler messageHandler,
                                ChatSessionUserService chatSessionUserService){
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
        return groupInfoMapper.findBatchByGroupOwnerId(userId);
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
}
