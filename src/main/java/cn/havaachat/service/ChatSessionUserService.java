package cn.havaachat.service;

/**
 * 会话用户管理
 */
public interface ChatSessionUserService {
    /**
     * 更新会话中的联系人昵称
     * @param contactId
     * @param contactName
     */
    void updateChatSessionUserName(String contactId,String contactName);
}
