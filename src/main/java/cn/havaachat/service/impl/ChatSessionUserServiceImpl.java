package cn.havaachat.service.impl;

import cn.havaachat.enums.MessageTypeEnum;
import cn.havaachat.enums.UserContactStatusEnum;
import cn.havaachat.enums.UserContactTypeEnum;
import cn.havaachat.mapper.ChatSessionUserMapper;
import cn.havaachat.mapper.UserContactMapper;
import cn.havaachat.pojo.dto.SendMessageToFrontDTO;
import cn.havaachat.pojo.entity.ChatSessionUser;
import cn.havaachat.pojo.entity.UserContact;
import cn.havaachat.service.ChatSessionUserService;
import cn.havaachat.websocket.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会话用户管理
 */
@Service
@Slf4j
public class ChatSessionUserServiceImpl implements ChatSessionUserService {
    private UserContactMapper userContactMapper;
    private MessageHandler messageHandler;
    private ChatSessionUserMapper chatSessionUserMapper;
    public ChatSessionUserServiceImpl(UserContactMapper userContactMapper,MessageHandler messageHandler,ChatSessionUserMapper chatSessionUserMapper){
        this.userContactMapper = userContactMapper;
        this.messageHandler = messageHandler;
        this.chatSessionUserMapper = chatSessionUserMapper;
    }

    /**
     * 更新会话中的联系人昵称
     * @param contactId
     * @param contactName
     */
    @Override
    public void updateChatSessionUserName(String contactId, String contactName) {
        UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getById(contactId);
        // 群昵称修改
        if (userContactTypeEnum==UserContactTypeEnum.GROUP){
            // 更新所有群员会话中的昵称信息
            ChatSessionUser chatSessionUserForUpdate = new ChatSessionUser();
            chatSessionUserForUpdate.setContactId(contactId);
            chatSessionUserForUpdate.setContactName(contactName);
            chatSessionUserMapper.updateByContactId(chatSessionUserForUpdate);
            // 向所有群员发送ws消息
            SendMessageToFrontDTO sendMessageToFrontDTO = new SendMessageToFrontDTO();
            sendMessageToFrontDTO.setContactId(contactId);
            sendMessageToFrontDTO.setContactType(UserContactTypeEnum.GROUP.getType());
            sendMessageToFrontDTO.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
            sendMessageToFrontDTO.setExtendData(contactName);
            messageHandler.sendMessage(sendMessageToFrontDTO);
        }else{ // 用户昵称修改
            // 更新该用户所有好友的会话中的昵称信息
            ChatSessionUser chatSessionUserForUpdate = new ChatSessionUser();
            chatSessionUserForUpdate.setContactId(contactId);
            chatSessionUserForUpdate.setContactName(contactName);
            chatSessionUserMapper.updateByContactId(chatSessionUserForUpdate);
            // 向该用户的所有好友发送ws消息
            List<UserContact> userContactList = userContactMapper.findBatchByUserIdAndContactTypeAndStatus(contactId, UserContactTypeEnum.USER.getType(), UserContactStatusEnum.FRIEND.getStatus());
            for (UserContact userContact : userContactList) {
                SendMessageToFrontDTO sendMessageToFrontDTO = new SendMessageToFrontDTO();
                sendMessageToFrontDTO.setSendUserId(contactId);
                sendMessageToFrontDTO.setSendUserNickName(contactName);
                sendMessageToFrontDTO.setContactId(userContact.getContactId());
                sendMessageToFrontDTO.setContactType(UserContactTypeEnum.USER.getType());
                sendMessageToFrontDTO.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
                sendMessageToFrontDTO.setExtendData(contactName);
                messageHandler.sendMessage(sendMessageToFrontDTO);
            }
        }
    }
}
