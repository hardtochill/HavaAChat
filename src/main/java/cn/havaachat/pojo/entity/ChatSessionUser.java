package cn.havaachat.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话用户
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionUser extends BaseEntity{
    /**
     * 用户id
     */
    private String userId;
    /**
     * 联系人id
     */
    private String contactId;
    /**
     * 会话id
     */
    private String sessionId;
    /**
     * 联系人名称
     */
    private String contactName;
    /**
     * 联系人发的最后一条消息
     */
    private String lastMessage;
    /**
     * 联系人发的最后一条消息的时间
     */
    private Long lastReceiveTime;
    /**
     * 如果是群聊，则还要查询群聊的人数
     */
    private Integer memberCount;
}
