package cn.havaachat.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession extends BaseEntity{
    /**
     * 会话id
     */
    private String sessionId;
    /**
     * 最后收到的消息
     */
    private String lastMessage;
    /**
     * 最后收到消息的时间
     */
    private Long lastReceiveTime;
}
