package cn.havaachat.service;

import cn.havaachat.pojo.dto.SaveGroupDTO;
import cn.havaachat.pojo.entity.GroupInfo;
import cn.havaachat.pojo.vo.GroupInfoVO;

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
     * 在聊天会话界面获取群聊详细信息
     * @param groupId
     * @return
     */
    GroupInfoVO getGroupInfo4Chat(String groupId);
}
