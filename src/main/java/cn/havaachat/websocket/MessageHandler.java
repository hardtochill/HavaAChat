package cn.havaachat.websocket;

import cn.havaachat.pojo.dto.MessageSendDTO;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

/**
 * 消息处理
 */
@Component
@Slf4j
public class MessageHandler {
    private static final String MESSAGE_TOPIC = "message:topic";
    private RedissonClient redissonClient;
    private ChannelContextUtils channelContextUtils;
    @Autowired
    public MessageHandler(RedissonClient redissonClient,ChannelContextUtils channelContextUtils){
        this.redissonClient = redissonClient;
        this.channelContextUtils = channelContextUtils;
    }

    /**
     * 服务端消息转发逻辑
     * 1.有多个后台服务器形成集群，每个后台服务器都通过自己的Redisson去监听其他服务器发往同一个Redis中的消息，即通过多个Redisson监听一个Redis实现服务器集群的通信
     * 2.服务器ServerA收到客户端clientA将Message发给客户端clientB的请求
     * 3.ServerA不是直接将Message通过Netty发给clientB，而是ServerA将Message通过自己的Redisson广播到服务器集群中
     * 4.集群所有服务器收到广播，都在自己的内存中查看是否有clientB的连接通道，若有则再通过Netty将Message发给clientB。
     * 即在集群化环境中可能会出现这样的情况：
     * （1）serverA收到clientA将Message发给clientB的请求，
     * （2）但是serverA只与clientA建立连接，没有与clientB建立连接，
     * （3）因此serverA需要将Message广播到集群中，由已经与clientB建立连接的server去实现发送
     */

    /**
     * 监听集群发布的消息
     */
    @PostConstruct
    public void listenMessage(){
        RTopic rTopic = redissonClient.getTopic(MESSAGE_TOPIC);
        // 监听通道
        rTopic.addListener(MessageSendDTO.class,(MessageSendDTO,sendDto)->{
            log.info("收到广播消息：{}", JSON.toJSON(sendDto));
            // 发送消息
            channelContextUtils.checkAndSendMessage(sendDto);
        });
    }

    /**
     * 此处是将消息发往整个集群
     * @param messageSendDTO
     */
    public void sendMessage(MessageSendDTO messageSendDTO){
        RTopic rTopic = redissonClient.getTopic(MESSAGE_TOPIC);
        // 向通道发布消息
        rTopic.publish(messageSendDTO);
    }
}
