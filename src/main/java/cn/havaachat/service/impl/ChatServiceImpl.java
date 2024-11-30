package cn.havaachat.service.impl;

import cn.havaachat.config.AppConfiguration;
import cn.havaachat.constants.AccountConstants;
import cn.havaachat.constants.FileConstants;
import cn.havaachat.context.BaseContext;
import cn.havaachat.enums.*;
import cn.havaachat.exception.BaseException;
import cn.havaachat.mapper.ChatMessageMapper;
import cn.havaachat.mapper.ChatSessionMapper;
import cn.havaachat.mapper.UserContactMapper;
import cn.havaachat.pojo.dto.*;
import cn.havaachat.pojo.entity.ChatMessage;
import cn.havaachat.pojo.entity.ChatSession;
import cn.havaachat.pojo.entity.ChatSessionUser;
import cn.havaachat.pojo.entity.UserContact;
import cn.havaachat.redis.RedisService;
import cn.havaachat.service.ChatService;
import cn.havaachat.utils.FilePathUtils;
import cn.havaachat.utils.ResponseUtils;
import cn.havaachat.utils.StringUtils;
import cn.havaachat.websocket.MessageHandler;
import jodd.util.ArraysUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.ResponseCache;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {
    private RedisService redisService;
    private ChatMessageMapper chatMessageMapper;
    private ChatSessionMapper chatSessionMapper;
    private MessageHandler messageHandler;
    private AppConfiguration appConfiguration;
    private UserContactMapper userContactMapper;
    public ChatServiceImpl(RedisService redisService,ChatMessageMapper chatMessageMapper,ChatSessionMapper chatSessionMapper,
                           MessageHandler messageHandler,AppConfiguration appConfiguration,UserContactMapper userContactMapper){
        this.redisService = redisService;
        this.chatMessageMapper = chatMessageMapper;
        this.chatSessionMapper = chatSessionMapper;
        this.messageHandler = messageHandler;
        this.appConfiguration = appConfiguration;
        this.userContactMapper = userContactMapper;
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
    /**
     * 上传文件
     * @param uploadFileDTO
     */
    public void uploadFile(UploadFileDTO uploadFileDTO){
        TokenUserInfoDTO tokenUserInfo = BaseContext.getTokenUserInfo();
        Long messageId = uploadFileDTO.getMessageId();
        log.info("上传文件：messageId={}",messageId);
        // 校验messageId
        ChatMessage existChatMessage = chatMessageMapper.findById(messageId);
        if (null==existChatMessage){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        // 校验文件发送者
        if (!existChatMessage.getSendUserId().equals(tokenUserInfo.getUserId())){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        // 获取文件后缀
        MultipartFile file = uploadFileDTO.getFile();
        MultipartFile coverFile = uploadFileDTO.getCover();
        String fileSuffix = StringUtils.getFileSuffix(file.getOriginalFilename());
        SysSettingDTO sysSetting = redisService.getSysSetting();
        // 校验文件后缀及类型
        if (StringUtils.isEmpty(fileSuffix)){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        // 校验图片文件
        if (ArraysUtil.contains(FileConstants.IMAGE_SUFFIX_ARRAY,fileSuffix.toLowerCase())
                && file.getSize()>sysSetting.getMaxImageSize()*FileConstants.FILE_SIZE_MB){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        // 校验视频文件
        if (ArraysUtil.contains(FileConstants.VIDEO_SUFFIX_ARRAY,fileSuffix.toLowerCase())
                && file.getSize()>sysSetting.getMaxVideoSize()*FileConstants.FILE_SIZE_MB){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        // 校验其它文件
        if (file.getSize()>sysSetting.getMaxFileSize()*FileConstants.FILE_SIZE_MB){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        // 文件本地存储
        String uploadFileFolderPath = FilePathUtils.generateUploadFileFolderPath(appConfiguration.getFileFolder(),StringUtils.transferLongToLocalDate(existChatMessage.getSendTime()));
        File uploadFileFolder = new File(uploadFileFolderPath);
        if (!uploadFileFolder.exists()){
            uploadFileFolder.mkdirs();
        }
        // 原始文件
        // 注意前端传过来的文件，其文件名不再是原始文件名，而是messageId，因此这里要从数据库中取上传的文件名
        String uploadFilePath = FilePathUtils.generateUploadFilePath(uploadFileFolderPath,messageId,existChatMessage.getFileName(),fileSuffix);
        // 缩略文件
        String coverUploadFilePath = FilePathUtils.generateCoverUploadFilePath(uploadFileFolderPath,messageId,existChatMessage.getFileName());
        try{
            file.transferTo(new File(uploadFilePath));
            coverFile.transferTo(new File(coverUploadFilePath));
        }catch (Exception e){
            log.error("存储上传文件失败");
            throw new BaseException(ResponseCodeEnum.CODE_500);
        }
        // 修改ChatMessage状态
        ChatMessage chatMessageForUpdate = new ChatMessage();
        chatMessageForUpdate.setMessageId(messageId);
        chatMessageForUpdate.setStatus(MessageStatusEnum.SENDED.getStatus());
        chatMessageMapper.update(chatMessageForUpdate);
        // 发送ws消息给联系人，即文件接收者
        SendMessageToFrontDTO sendMessageToFrontDTO = new SendMessageToFrontDTO();
        sendMessageToFrontDTO.setMessageId(messageId);
        sendMessageToFrontDTO.setMessageType(MessageTypeEnum.FILE_UPLOAD.getType());
        sendMessageToFrontDTO.setStatus(MessageStatusEnum.SENDED.getStatus());
        sendMessageToFrontDTO.setContactId(existChatMessage.getContactId());
        messageHandler.sendMessage(sendMessageToFrontDTO);
    }

    /**
     * 下载文件
     * @param downloadFileDTO
     */
    @Override
    public void downloadFile(DownloadFileDTO downloadFileDTO) {
        log.info("下载文件：{}",downloadFileDTO);
        String fileId = downloadFileDTO.getFileId();
        Boolean showCover = downloadFileDTO.getShowCover();
        File file;
        // 取出本地头像
        if(!StringUtils.isNumeric(fileId)){ // 获取头像文件
            String avatarFileFolderPath = FilePathUtils.generateAvatarFileFolderPath(appConfiguration.getFileFolder());
            // 获取原图或是缩略图
            String avatarFilePath = showCover?FilePathUtils.generateCoverAvatarFilePath(avatarFileFolderPath,fileId):FilePathUtils.generateAvatarFilePath(avatarFileFolderPath,fileId);
            file = new File(avatarFilePath);
        }else{ // 获取聊天文件
            // 对于聊天文件，前端上传的fileId就是messageId
            file = downloadChatFile(Long.valueOf(fileId),showCover);
        }
        if (!file.exists()){
            throw new BaseException(ResponseCodeEnum.CODE_602);
        }
        // 获取本次请求的Response
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = requestAttributes.getResponse();
        if (null==response){
            log.error("下载文件失败");
            throw new BaseException(ResponseCodeEnum.CODE_500);
        }
        response.setContentType("application/x-msdownload;charset=UTF-8");
        response.setHeader("Content-Disposition","attachment;");
        response.setContentLengthLong(file.length());
        try(
                FileInputStream fileInputStream = new FileInputStream(file);
                OutputStream outputStream = response.getOutputStream();
                ){
            // 缓冲区
            byte[] byteData = new byte[1024];
            // 读取本地文件数据，输出到响应数据流中
            int length;
            while((length=fileInputStream.read(byteData))!=-1){
                outputStream.write(byteData,0,length);
            }
            outputStream.flush();
        }catch (Exception e){
            log.error("下载文件失败",e);
            throw new BaseException(ResponseCodeEnum.CODE_500);
        }
    }

    /**
     * 下载聊天文件
     * @param messageId
     * @param showCover
     * @return
     */
    public File downloadChatFile(Long messageId,Boolean showCover){
        String userId = BaseContext.getTokenUserInfo().getUserId();
        ChatMessage chatMessage = chatMessageMapper.findById(messageId);
        UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getById(chatMessage.getContactId());
        // 若消息是发给用户的，则消息的contactId就是本次请求的userId
        if (userContactTypeEnum==UserContactTypeEnum.USER && !chatMessage.getContactId().equals(userId)){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        // 若消息是发给群聊的，则本次请求的user必须要在群聊中
        if (userContactTypeEnum==UserContactTypeEnum.GROUP){
            UserContact existUserContact = userContactMapper.findByUserIdAndContactIdAndTypeAndStatus(userId, chatMessage.getContactId(), UserContactTypeEnum.GROUP.getType(), UserContactStatusEnum.FRIEND.getStatus());
            if (null==existUserContact){
                throw new BaseException(ResponseCodeEnum.CODE_600);
            }
        }
        // 获取本地文件
        String uploadFileFolderPath = FilePathUtils.generateUploadFileFolderPath(appConfiguration.getFileFolder(), StringUtils.transferLongToLocalDate(chatMessage.getSendTime()));
        // 原文件或缩略图
        String fileName = chatMessage.getFileName();
        String filePath = showCover?
               FilePathUtils.generateCoverUploadFilePath(uploadFileFolderPath,messageId,fileName):
               FilePathUtils.generateUploadFilePath(uploadFileFolderPath,messageId,fileName,StringUtils.getFileSuffix(fileName));
        return new File(filePath);
    }
}
