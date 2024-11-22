package cn.havaachat.websocket.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 心跳超时处理器
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class HeartbeatHandler extends ChannelDuplexHandler {
    /**
     * 心跳超时事件处理
     * （1）ChannelPipeline中添加的IdleStateHandler将在被触发时发送一个IdleStateEvent事件
     * （2）而userEventTriggered()专门用于处理IdleStateEvent事件
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if(idleStateEvent.state()== IdleState.READER_IDLE){
                // 用户心跳超时
                Channel channel = ctx.channel();
                Attribute<String> channelAttribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
                String userId = channelAttribute.get();
                log.info("netty:用户心跳超时，userId={}",userId);
                ctx.close();
                // 断开连接后会触发channelInactive()，该类的channelInactive()默认实现是向后面的ChannelInboundHandler传递
            }else if(idleStateEvent.state() == IdleState.WRITER_IDLE){
                ctx.writeAndFlush("heart");
            }
        }
    }
}
