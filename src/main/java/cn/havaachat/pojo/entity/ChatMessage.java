package cn.havaachat.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天信息表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage extends BaseEntity{
    /**
     * 消息自增id
     */
    private Long messageId;
    /**
     * 会话id
     */
    private String sessionId;
    /**
     * 消息类型
     */
    private Integer messageType;
    /**
     * 消息内容
     */
    private String messageContent;
    /**
     * 发送人id
     */
    private String sendUserId;
    /**
     * 发送人昵称
     */
    private String sendUserNickName;
    /**
     * 发送时间
     */
    private Long sendTime;
    /**
     * 接收消息的联系人id
     */
    private String contactId;
    /**
     * 联系人类型 0：单聊，1：群聊
     */
    private Integer contactType;
    /**
     * 文件大小
     */
    private Long fileSize;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 文件类型
     */
    private Integer fileType;
    /**
     * 消息状态 0：发送中，1：已发送
     * 对于文件消息，使用异步上传，通过消息状态处理
     */
    private Integer status;
}
