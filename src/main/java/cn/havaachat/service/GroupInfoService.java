package cn.havaachat.service;

import cn.havaachat.enums.MessageTypeEnum;
import cn.havaachat.pojo.dto.AddOrRemoveGroupUserDTO;
import cn.havaachat.pojo.dto.PageDTO;
import cn.havaachat.pojo.dto.SaveGroupDTO;
import cn.havaachat.pojo.entity.GroupInfo;
import cn.havaachat.pojo.vo.GroupInfoVO;
import cn.havaachat.pojo.vo.PageResultVO;

import java.io.IOException;
import java.util.List;

public interface GroupInfoService {
    /**
     * 增或修改群组
     * @param saveGroupDTO
     */
    void saveGroupInfo(SaveGroupDTO saveGroupDTO) throws IOException;

    /**
     * 获取当前用户创建的群聊
     * @param userId
     * @return
     */
    List<GroupInfo> loadMyGroup(String userId);

    /**
     * 获取群聊详细信息
     * @param groupId
     * @return
     */
    GroupInfo getGroupInfo(String groupId);
    /**
     * 获取群组列表
     * @param pageDTO
     * @param groupId
     * @param groupNameFuzzy
     * @param groupOwnerId
     * @return
     */
    PageResultVO loadGroup(PageDTO pageDTO, String groupId, String groupNameFuzzy, String groupOwnerId);

    /**
     * 解散群组
     * @param groupId
     */
    void dissolutionGroup(String groupId);

    /**
     * 在聊天会话界面获取群聊详细信息
     * @param groupId
     * @return
     */
    GroupInfoVO getGroupInfo4Chat(String groupId);

    /**
     * 退出群聊（主动或被动）
     * @param userId
     * @param groupId
     * @param messageTypeEnum
     */
    void leaveGroup(String userId, String groupId, MessageTypeEnum messageTypeEnum);

    /**
     * 添加或移除群成员
     * @param addOrRemoveGroupUserDTO
     */
    void addOrRemoveGroupUser(AddOrRemoveGroupUserDTO addOrRemoveGroupUserDTO);
}
