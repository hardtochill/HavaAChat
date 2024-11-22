package cn.havaachat.pojo.dto;

import cn.havaachat.pojo.entity.ChatMessage;
import cn.havaachat.pojo.entity.ChatSessionUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户聊天界面初始化
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WsInitDataDTO {
    /**
     * 会话用户列表
     */
    private List<ChatSessionUser> chatSessionList;
    /**
     * 离线时收到的信息
     */
    private List<ChatMessage>  chatMessageList;
    /**
     * 收到的好友申请数量
     */
    private Integer applyCount;
}
