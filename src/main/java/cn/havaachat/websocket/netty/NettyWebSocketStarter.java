package cn.havaachat.websocket.netty;

import cn.havaachat.config.AppConfiguration;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
@Component
@Slf4j
public class NettyWebSocketStarter implements Runnable{
    private static NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private static NioEventLoopGroup workerGroup = new NioEventLoopGroup();
    private HeartbeatHandler heartbeatHandler;
    private WebSocketHandler webSocketHandler;
    private AppConfiguration appConfiguration;
    @Autowired
    public NettyWebSocketStarter(HeartbeatHandler heartbeatHandler,WebSocketHandler webSocketHandler,AppConfiguration appConfiguration){
        this.heartbeatHandler = heartbeatHandler;
        this.webSocketHandler = webSocketHandler;
        this.appConfiguration = appConfiguration;
    }
    public void run(){
        startNetty();
    }

    public void startNetty(){
        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class)
                    .group(bossGroup,workerGroup)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            // 1.加入http编解码器，负责将接收到的字节流解码为HttpObject（例如HttpRequest或HttpContent），以及HttpObject编码为字节流以发送给客户端
                            pipeline.addLast(new HttpServerCodec());
                            // 2.负责将多个HttpObject聚合成完整的FullHttpObject。在HTTP协议中，一个请求或响应可能由多个部分（称为“块”或“帧”）组成，HttpObjectAggregator 负责将这些部分组合成一个完整的消息。
                            pipeline.addLast(new HttpObjectAggregator(64*1024));
                            // 3.监控连接的空闲状态，相当于“心跳检测”，参数：
                            // (1)对客户端的读空闲时间：如果在指定的时间内没有从连接中读取到任何数据，则触发一个READER_IDLE_STATE_EVENT，即读超时事件。
                            // (2)对客户端的写空闲时间：如果在指定的时间内没有向连接中写入任何数据，则触发一个WRITER_IDLE_STATE_EVENT，即写超时事件。这里被设置为0，意味着不会检测写空闲状态。
                            // (3)对客户端的读写空闲时间：如果在指定的时间内连接既没有读操作也没有写操作，则触发一个ALL_IDLE_STATE_EVENT，即读写超时事件。这里也被设置为0，意味着不会检测读写空闲状态。
                            // (4)时间单位
                            pipeline.addLast(new IdleStateHandler(6,0,0, TimeUnit.SECONDS));
                            // 4.心跳超时处理器，针对上述的心跳检测超时触发的IDLE_STATE_EVENT进行处理
                            pipeline.addLast(heartbeatHandler);
                            // 5.将http协议升级到websocket协议
                            pipeline.addLast(new WebSocketServerProtocolHandler(appConfiguration.getWsPath(),null,true,64*1024,true,true,10000L));
                            // 6.业务逻辑处理
                            pipeline.addLast(webSocketHandler);
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(appConfiguration.getWsPort()).sync();
            log.info("netty:服务启动成功，端口号={}",appConfiguration.getWsPort());
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            log.error("netty:服务启动失败",e);
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            log.info("netty:资源释放成功");
        }
    }
}
