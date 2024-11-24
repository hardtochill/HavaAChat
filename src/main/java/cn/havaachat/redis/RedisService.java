package cn.havaachat.redis;

import cn.havaachat.pojo.dto.SysSettingDTO;
import cn.havaachat.pojo.dto.TokenUserInfoDTO;

import java.util.List;

/**
 * redis业务类
 */
public interface RedisService {
    /**
     * 获取用户心跳
     * @param userId
     * @return
     */
    Long getUserHeartBeat(String userId);
    /**
     * 存储用户心跳
     * @param userId
     * @return
     */
    void saveUserHeartBeat(String userId);
    /**
     * 移除用户心跳
     * @param userId
     * @return
     */
    void removeUserHeartBeat(String userId);

    /**
     * 将TokenUserInfoDTO和token存入redis
     * 后续根据userId拿到redis中的token，再根据token拿到redis中的TokenUserInfoDTO
     * @param tokenUserInfoDTO
     */
    void saveTokenUserInfoDTOAndToken(TokenUserInfoDTO tokenUserInfoDTO);

    /**
     * 获取用户token
     * @param token
     */
    TokenUserInfoDTO getTokenUserInfoDTO(String token);

    /**
     * 根据userId清除用户在redis中的token信息
     * @param userId
     */
    void cleanTokenUserInfoDTOAndTokenByUserId(String userId);
    /**
     * 获取系统设置
     * @return
     */
    SysSettingDTO getSysSetting();

    /**
     * 保存系统设置
     * @param sysSettingDTO
     */
    void saveSysSetting(SysSettingDTO sysSettingDTO);

    /**
     * 清空用户联系人
     * @param userId
     */
    void cleanUserContact(String userId);
    /**
     * 批量添加用户联系人
     * @param userId
     * @param userContactIdList
     */
    void saveUserContactIdList(String userId, List<String> userContactIdList);
    /**
     * 获取用户联系人列表
     * @param userId
     */
    List<String> getUserContactIdList(String userId);


}
