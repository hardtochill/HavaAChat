package cn.havaachat.websocket.netty;

import cn.havaachat.pojo.dto.TokenUserInfoDTO;
import cn.havaachat.redis.RedisService;
import cn.havaachat.utils.StringUtils;
import cn.havaachat.websocket.ChannelContextUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 业务处理
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private RedisService redisService;
    private ChannelContextUtils channelContextUtils;
    @Autowired
    public WebSocketHandler(RedisService redisService,ChannelContextUtils channelContextUtils){
        this.redisService = redisService;
        this.channelContextUtils = channelContextUtils;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("netty:有新的连接加入......");
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("netty:有连接断开......");
        // 执行断开连接操作
        channelContextUtils.removeContext(ctx.channel());
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame){
        Channel channel = ctx.channel();
        // 获取该channel的userId
        String userId = channelContextUtils.getUserIdByChannel(channel);
        //log.info("netty:收到消息：userId={}，message=\"{}\"",userId,textWebSocketFrame.text());
        // 更新用户心跳
        redisService.saveUserHeartBeat(userId);
    }

    /**
     * websocket协议握手完成时触发
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt){
        if(evt instanceof WebSocketServerProtocolHandler.HandshakeComplete){
            WebSocketServerProtocolHandler.HandshakeComplete handshakeComplete = (WebSocketServerProtocolHandler.HandshakeComplete)evt;
            String url = handshakeComplete.requestUri();
            log.info("netty:WebSocket握手完成，url={}",url);
            // 获取token
            String token = StringUtils.getTokenInWebSocketUrl(url);
            if (null==token){
                log.error("netty:token为null，断开连接");
                ctx.close();
                return;
            }
            // 校验token
            TokenUserInfoDTO tokenUserInfoDTO = redisService.getTokenUserInfoDTOByToken(token);
            if(null==tokenUserInfoDTO){
                log.error("netty:token不存在，断开连接");
                ctx.close();
                return;
            }
            // 为该channel进行初始化
            channelContextUtils.addContext(tokenUserInfoDTO.getUserId(),ctx.channel());
        }
    }
}
