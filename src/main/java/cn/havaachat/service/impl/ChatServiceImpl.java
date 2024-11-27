package cn.havaachat.service.impl;

import cn.havaachat.constants.AccountConstants;
import cn.havaachat.context.BaseContext;
import cn.havaachat.enums.MessageStatusEnum;
import cn.havaachat.enums.MessageTypeEnum;
import cn.havaachat.enums.ResponseCodeEnum;
import cn.havaachat.enums.UserContactTypeEnum;
import cn.havaachat.exception.BaseException;
import cn.havaachat.mapper.ChatMessageMapper;
import cn.havaachat.mapper.ChatSessionMapper;
import cn.havaachat.pojo.dto.*;
import cn.havaachat.pojo.entity.ChatMessage;
import cn.havaachat.pojo.entity.ChatSession;
import cn.havaachat.pojo.entity.ChatSessionUser;
import cn.havaachat.redis.RedisService;
import cn.havaachat.service.ChatService;
import cn.havaachat.utils.StringUtils;
import cn.havaachat.websocket.MessageHandler;
import jodd.util.ArraysUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {
    private RedisService redisService;
    private ChatMessageMapper chatMessageMapper;
    private ChatSessionMapper chatSessionMapper;
    private MessageHandler messageHandler;
    public ChatServiceImpl(RedisService redisService,ChatMessageMapper chatMessageMapper,ChatSessionMapper chatSessionMapper,
                           MessageHandler messageHandler){
        this.redisService = redisService;
        this.chatMessageMapper = chatMessageMapper;
        this.chatSessionMapper = chatSessionMapper;
        this.messageHandler = messageHandler;
    }
    /**
     * 发送消息
     * @param sendMessageToBackendDTO
     */
    @Override
    @Transactional
    public SendMessageToFrontDTO sendMessage(SendMessageToBackendDTO sendMessageToBackendDTO) {
        log.info("消息发送：{}",sendMessageToBackendDTO);
        TokenUserInfoDTO tokenUserInfo = BaseContext.getTokenUserInfo();
        // 校验消息类型，要发送的消息只可能是两种：聊天或媒体文件
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(sendMessageToBackendDTO.getMessageType());
        if (null==messageTypeEnum || !ArraysUtil.contains(new int[]{MessageTypeEnum.CHAT.getType(),MessageTypeEnum.MEDIA_CHAT.getType()},messageTypeEnum.getType())){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        String contactId = sendMessageToBackendDTO.getContactId();
        // 判断联系人是用户还是群聊
        UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getById(contactId);
        if (null==userContactTypeEnum){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        // 校验是否存在联系关系，对于机器人收发消息无需校验
        if (!AccountConstants.ROBOT_UID.equals(contactId) && !AccountConstants.ROBOT_UID.equals(tokenUserInfo.getUserId())){
            // 从缓存中取出用户联系人
            List<String> userContactIdList = redisService.getUserContactIdList(tokenUserInfo.getUserId());
            if (!userContactIdList.contains(contactId)){
                if (UserContactTypeEnum.USER == userContactTypeEnum) {
                    throw new BaseException(ResponseCodeEnum.CODE_902);
                }else{
                    throw new BaseException(ResponseCodeEnum.CODE_903);
                }
            }
        }
        // todo 校验是否被对方删除或拉黑

        Long now = System.currentTimeMillis();

        // 保存ChatMessage
        ChatMessage chatMessage = new ChatMessage();
        BeanUtils.copyProperties(sendMessageToBackendDTO,chatMessage);
        String sessionId;
        if (UserContactTypeEnum.USER==userContactTypeEnum){
            sessionId = StringUtils.getChatSessionIdForUser(new String[]{tokenUserInfo.getUserId(),contactId});
        }else{
            sessionId = StringUtils.getChatSessionIdForGroup(contactId);
        }
        chatMessage.setSessionId(sessionId);
        chatMessage.setSendUserId(tokenUserInfo.getUserId());
        chatMessage.setSendUserNickName(tokenUserInfo.getNickName());
        chatMessage.setSendTime(now);
        chatMessage.setContactType(userContactTypeEnum.getType());
        // 聊天信息的消息状态为SENDED，媒体文件的消息状态为SENDING
        Integer status = MessageTypeEnum.CHAT==messageTypeEnum? MessageStatusEnum.SENDED.getStatus() : MessageStatusEnum.SENDING.getStatus();
        chatMessage.setStatus(status);
        chatMessageMapper.insert(chatMessage);

        // 更新ChatSession
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        String lastMessage = sendMessageToBackendDTO.getMessageContent();
        // 如果是群聊，则最后一条消息要加上联系人昵称
        if (UserContactTypeEnum.GROUP==userContactTypeEnum){
            lastMessage = tokenUserInfo.getNickName()+"："+lastMessage;
        }
        chatSession.setLastMessage(lastMessage);
        chatSession.setLastReceiveTime(now);
        chatSessionMapper.update(chatSession);

        // 发送ws消息
        SendMessageToFrontDTO sendMessageToFrontDTO = new SendMessageToFrontDTO();
        BeanUtils.copyProperties(chatMessage,sendMessageToFrontDTO);
        // 机器人自动回复 todo 此处可以接入大模型接口
        if(AccountConstants.ROBOT_UID.equals(contactId)){
            SysSettingDTO sysSettingDTO = redisService.getSysSetting();
            // 为机器人创建token
            TokenUserInfoDTO robotTokenUserInfoDTO = new TokenUserInfoDTO();
            robotTokenUserInfoDTO.setUserId(sysSettingDTO.getRobotUid());
            robotTokenUserInfoDTO.setNickName(sysSettingDTO.getRobotNickName());
            SendMessageToBackendDTO robotMessageDTO = new SendMessageToBackendDTO();
            // 对机器人来说，联系人就是用户自己
            robotMessageDTO.setContactId(tokenUserInfo.getUserId());
            robotMessageDTO.setMessageType(MessageTypeEnum.CHAT.getType());
            robotMessageDTO.setMessageContent(sysSettingDTO.getRobotAutoResponse());
            // 将线程中缓存的tokenUserInfo换成robot的
            BaseContext.removeTokenUserInfo();
            BaseContext.setTokenUserInfo(robotTokenUserInfoDTO);
            sendMessage(robotMessageDTO);
        }else{
            messageHandler.sendMessage(sendMessageToFrontDTO);
        }
        // 返回sendMessageToFrontDTO用于消息发送者的前端渲染展示自己发送的消息
        return sendMessageToFrontDTO;
    }

}
