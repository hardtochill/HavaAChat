package cn.havaachat.pojo.dto;

import cn.havaachat.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息发送DTO，后端发给前端
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendMessageToFrontDTO<T> {
    /**
     * 消息id
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
     * 消息发送时间
     */
    private Long sendTime;
    /**
     * 联系人id
     */
    private String contactId;
    /**
     * 联系人类型 0：用户，1：群聊
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
    /**
     * 群人数
     */
    private Integer memberCount;
    /**
     * 联系人名称
     */
    private String contactName;
    /**
     * 最后一条消息
     */
    private String lastMessage;
    /**
     * 拓展消息
     */
    private T extendData;

    public String getLastMessage(){
        return StringUtils.isEmpty(lastMessage)?messageContent:lastMessage;
    }
}
