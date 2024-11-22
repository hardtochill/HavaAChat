package cn.havaachat;

import cn.havaachat.websocket.netty.NettyWebSocketStarter;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InitRun implements ApplicationRunner {
    private NettyWebSocketStarter nettyWebSocketStarter;
    public InitRun(NettyWebSocketStarter nettyWebSocketStarter){
        this.nettyWebSocketStarter = nettyWebSocketStarter;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 异步启动Netty
        new Thread(nettyWebSocketStarter).start();
    }
}
