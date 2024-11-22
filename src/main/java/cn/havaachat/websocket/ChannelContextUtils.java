package cn.havaachat.websocket;

import cn.havaachat.constants.ContactConstants;
import cn.havaachat.enums.MessageTypeEnum;
import cn.havaachat.enums.UserContactTypeEnum;
import cn.havaachat.mapper.ChatSessionUserMapper;
import cn.havaachat.mapper.UserInfoMapper;
import cn.havaachat.pojo.dto.MessageSendDTO;
import cn.havaachat.pojo.dto.WsInitDataDTO;
import cn.havaachat.pojo.entity.ChatSessionUser;
import cn.havaachat.pojo.entity.UserInfo;
import cn.havaachat.redis.RedisService;
import cn.havaachat.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Netty的Channel工具类
 */
@Component
@Slf4j
public class ChannelContextUtils {
    /**
     * 由于Channel对象无法序列化，无法存入Redis；因此定义线程安全的ConcurrentHashMap进行全局存储
     */
    // key是userId，value是该userId对应的channel
    private static final ConcurrentHashMap<String,Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();
    // key是groupId，value是一个ChannelGroup，里面存储了所有群员的channel
    private static final ConcurrentHashMap<String, ChannelGroup> GROUP_CONTEXT_MAP = new ConcurrentHashMap<>();
   private RedisService redisService;
   private UserInfoMapper userInfoMapper;
   private ChatSessionUserMapper chatSessionUserMapper;
   @Autowired
   public ChannelContextUtils(RedisService redisService,UserInfoMapper userInfoMapper,ChatSessionUserMapper chatSessionUserMapper){
       this.redisService = redisService;
       this.userInfoMapper = userInfoMapper;
       this.chatSessionUserMapper = chatSessionUserMapper;
   }

    /**
     * 为每一个刚建立连接的Channel做初始化
     * @param userId
     * @param channel
     */
    public void addContext(String userId, Channel channel){
        // 将channel绑定当前连接的userId
        String channelId = channel.id().toString();
        AttributeKey attributeKey = null;
        if(AttributeKey.exists(channelId)){
            attributeKey = AttributeKey.valueOf(channelId);
        }else{
            attributeKey = AttributeKey.newInstance(channelId);
        }
        channel.attr(attributeKey).set(userId);

        // 将channel存入全局
        USER_CONTEXT_MAP.put(userId,channel);

        // 刚建立连接时就更新redis中的心跳
        redisService.saveUserHeartBeat(userId);

        // 从redis中查询用户所有联系人，并将用户加入其所有群组的ChannelGroup，使其能接收到所有群组的信息
        List<String> userContactIdList = redisService.getUserContactIdList(userId);
        for (String contactId : userContactIdList) {
            if (contactId.startsWith(UserContactTypeEnum.GROUP.getPrefix())){
                addUser2Group(userId,contactId);
            }
        }

        // 更新用户最后登录时间
        UserInfo userInfoForUpdate = new UserInfo();
        userInfoForUpdate.setUserId(userId);
        userInfoForUpdate.setLastLoginTime(LocalDateTime.now());
        userInfoMapper.update(userInfoForUpdate);

        /**
         * 用户刚登陆后要向用户展示：1.会话用户列表、2.离线时收到的消息列表、3.收到的好友申请数量
         * 保证了用户换设备后仍能同步消息
         */
        WsInitDataDTO wsInitDataDTO = new WsInitDataDTO();

        // 1.查询会话用户列表
        List<ChatSessionUser> chatSessionUserList = chatSessionUserMapper.findBatchWithSessionByUserId(userId);
        wsInitDataDTO.setChatSessionList(chatSessionUserList);

        // 2.查询用户离线时收到的消息
        // 自最后一次离线后，离最近3天收到的消息；即若用户一年未登录，则再次登陆时只向其发送最近3天收到的消息
        UserInfo userInfo = userInfoMapper.findById(userId);
        // 若用户离线时间未超过3天，则以用户离线时间为查询时间点
        Long lastOffTime = userInfo.getLastOffTime();
        // 若用户离线时间超过3天，则以最近3天为查询时间点
        if (userInfo.getLastOffTime()!=null && System.currentTimeMillis()-userInfo.getLastOffTime()> ContactConstants.MILLION_SECONDS_3_DAY){
            lastOffTime = ContactConstants.MILLION_SECONDS_3_DAY;
        }

        // 3.查询用户收到的好友申请数量

        // 向刚登陆用户发送初始信息
        MessageSendDTO messageSendDTO = new MessageSendDTO();
        messageSendDTO.setMessageType(MessageTypeEnum.INIT.getType());
        messageSendDTO.setContactId(userId);
        messageSendDTO.setExtendData(wsInitDataDTO);
        sendMessage(messageSendDTO,userId);
    }

    /**
     * 向目标对象发送消息
     * @param messageSendDTO
     * @param receiveId
     */
    public static void sendMessage(MessageSendDTO messageSendDTO,String receiveId){
        if (null==receiveId){
            return;
        }
        // 服务端与消息接收方的channel
        Channel receiveChannel = USER_CONTEXT_MAP.get(receiveId);
        if (null==receiveChannel){
            return;
        }
        // A给B发消息，对于消息接收者B来说，这条消息的发送者A就是联系人
        messageSendDTO.setContactId(messageSendDTO.getSendUserId());
        messageSendDTO.setContactName(messageSendDTO.getSendUserNickName());
        receiveChannel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(messageSendDTO)));
    }

    /***
     * 将用户加入其所有的GroupChannel，使其能接收到群聊信息
     * @param userId
     * @param groupId
     */
    public void addUser2Group(String userId,String groupId){
        Channel userChannel = USER_CONTEXT_MAP.get(userId);
        add2Group(groupId,userChannel);
    }
    public void add2Group(String groupId,Channel context){
        ChannelGroup groupChannel = GROUP_CONTEXT_MAP.get(groupId);
        if(null==groupChannel){
            groupChannel = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CONTEXT_MAP.put(groupId,groupChannel);
        }
        if(null==context){
            return;
        }
        groupChannel.add(context);
    }

    /**
     * 为断开连接的channel执行操作
     * @param channel
     */
    public void removeContext(Channel channel){
        String userId = getUserIdByChannel(channel);
        if (StringUtils.isEmpty(userId)){
            return;
        }
        // 将其channel从全局缓存中移除
        USER_CONTEXT_MAP.remove(userId);
        // 将用户心跳从redis中移除
        redisService.saveUserHeartBeat(userId);
        // 更新用户最后离线时间
        UserInfo userInfoForUpdate = new UserInfo();
        userInfoForUpdate.setUserId(userId);
        userInfoForUpdate.setLastOffTime(System.currentTimeMillis());
        userInfoMapper.update(userInfoForUpdate);
    }

    /**
     * 根据channel获取到其绑定的userId
     * @param channel
     * @return
     */
    public String getUserIdByChannel(Channel channel){
        Attribute<String> channelAttribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        return channelAttribute.get();
    }
}
