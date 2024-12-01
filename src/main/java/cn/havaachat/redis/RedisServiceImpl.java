package cn.havaachat.redis;

import cn.havaachat.constants.RedisConstants;
import cn.havaachat.pojo.dto.SysSettingDTO;
import cn.havaachat.pojo.dto.TokenUserInfoDTO;
import cn.havaachat.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 涉及redis的业务代码
 */
@Service
@Slf4j
public class RedisServiceImpl implements RedisService{
    private RedisUtils redisUtils;
    @Autowired
    public RedisServiceImpl(RedisUtils redisUtils){
        this.redisUtils = redisUtils;
    }

    /**
     * 获取用户心跳
     * @param userId
     * @return
     */
    public Long getUserHeartBeat(String userId){
        log.info("获取用户心跳：userId={}",userId);
        return (Long)redisUtils.get(StringUtils.getRedisWsUserHeartbeatKeyByUserId(userId));
    }

    /**
     * 存储用户心跳
     * @param userId
     */
    public void saveUserHeartBeat(String userId){
        //log.info("存储用户心跳：userId={}",userId);
        redisUtils.set(StringUtils.getRedisWsUserHeartbeatKeyByUserId(userId),System.currentTimeMillis(),RedisConstants.REDIS_KEY_EXPIRES_HEART_BEAT);
    }

    /**
     * 移除用户心跳
     * @param userId
     */
    @Override
    public void removeUserHeartBeat(String userId) {
        log.info("移除用户心跳：userId={}",userId);
        redisUtils.del(StringUtils.getRedisWsUserHeartbeatKeyByUserId(userId));
    }

    /**
     * 将TokenUserInfoDTO和token存入redis
     * 后续根据userId拿到redis中的token，再根据token拿到redis中的TokenUserInfoDTO
     * @param tokenUserInfoDTO
     */
    public void saveTokenUserInfoDTOAndToken(TokenUserInfoDTO tokenUserInfoDTO){
        log.info("将TokenUserInfoDTO和token存入redis：{}",tokenUserInfoDTO);
        // tokenUserInfoKey:tokenUserInfoDTO
        redisUtils.set(StringUtils.getRedisTokenUserInfoDTOKeyByToken(tokenUserInfoDTO.getToken()),tokenUserInfoDTO,RedisConstants.REDIS_KEY_EXPIRES_TOKEN);
        // tokenUserIdKey:token
        redisUtils.set(StringUtils.getRedisTokenKeyByUserId(tokenUserInfoDTO.getUserId()),tokenUserInfoDTO.getToken(),RedisConstants.REDIS_KEY_EXPIRES_TOKEN);
    }
    /**
     * 将TokenUserInfoDTO存入redis
     * @param tokenUserInfoDTO
     */
    public void saveTokenUserInfoDTO(TokenUserInfoDTO tokenUserInfoDTO){
        log.info("将TokenUserInfoDTO存入redis：{}",tokenUserInfoDTO);
        // tokenUserInfoKey:tokenUserInfoDTO
        redisUtils.set(StringUtils.getRedisTokenUserInfoDTOKeyByToken(tokenUserInfoDTO.getToken()),tokenUserInfoDTO,RedisConstants.REDIS_KEY_EXPIRES_TOKEN);
    }
    /**
     * 清除用户在redis中存储的token信息
     * @param userId
     */
    @Override
    public void cleanTokenUserInfoDTOAndTokenByUserId(String userId) {
        // 先根据userId取出token
        String token = (String)redisUtils.get(StringUtils.getRedisTokenKeyByUserId(userId));
        if (StringUtils.isEmpty(token)){
            return;
        }
        // 再根据token删除tokenUserInfo
        redisUtils.del(StringUtils.getRedisTokenUserInfoDTOKeyByToken(token));
        // 再删除token
        redisUtils.del(StringUtils.getRedisTokenKeyByUserId(userId));
    }

    /**
     * 获取用户token获取其tokenUserInfoDTO
     * @param token
     */
    @Override
    public TokenUserInfoDTO getTokenUserInfoDTOByToken(String token) {
        return (TokenUserInfoDTO) redisUtils.get(StringUtils.getRedisTokenUserInfoDTOKeyByToken(token));
    }

    /**
     * 获取用户id获取其tokenUserInfoDTO
     * @param userId
     * @return
     */
    @Override
    public TokenUserInfoDTO getTokenUserInfoDTOByUserId(String userId) {
        String token = (String)redisUtils.get(StringUtils.getRedisTokenKeyByUserId(userId));
        return getTokenUserInfoDTOByToken(token);
    }

    /**
     * 获取系统设置
     * @return
     */
    public SysSettingDTO getSysSetting(){
        SysSettingDTO sysSettingDTO = (SysSettingDTO)redisUtils.get(RedisConstants.REDIS_KEY_SYE_SETTING);
        return null==sysSettingDTO?new SysSettingDTO():sysSettingDTO;
    }

    /**
     * 保存系统设置
     * @param sysSettingDTO
     */
    public void saveSysSetting(SysSettingDTO sysSettingDTO){
        redisUtils.set(RedisConstants.REDIS_KEY_SYE_SETTING,sysSettingDTO);
    }

    /**
     * 清空用户联系人
     * @param userId
     */
    @Override
    public void cleanUserContact(String userId) {
        redisUtils.del(StringUtils.getRedisUserContactIdListKeyByUserId(userId));
    }

    /**
     * 批量添加用户联系人
     * @param userId
     * @param userContactIdList
     */
    @Override
    public void saveUserContactIdList(String userId, List<String> userContactIdList) {
        String redisUserContactKey = StringUtils.getRedisUserContactIdListKeyByUserId(userId);
        redisUtils.lSetAll(redisUserContactKey,userContactIdList,RedisConstants.REDIS_KEY_EXPIRES_TOKEN);
    }

    /**
     * 添加单个用户联系人
     * @param userId
     * @param userContactId
     */
    @Override
    public void saveUserContactId(String userId, String userContactId) {
        List<String> userContactIdList = getUserContactIdList(userId);
        if (userContactIdList.contains(userContactId)){
            return;
        }
        redisUtils.lSet(StringUtils.getRedisUserContactIdListKeyByUserId(userId),userContactId,RedisConstants.REDIS_KEY_EXPIRES_TOKEN);
    }
    /**
     * 移除单个用户联系人
     * @param userId
     * @param userContactId
     */
    public void removeUserContactId(String userId,String userContactId){
        redisUtils.lRemove(StringUtils.getRedisUserContactIdListKeyByUserId(userId),1,userContactId);
    }
    /**
     * 获取用户联系人列表
     * @param userId
     * @return
     */
    @Override
    public List<String> getUserContactIdList(String userId) {
        List<Object> objectList = redisUtils.lGet(StringUtils.getRedisUserContactIdListKeyByUserId(userId), 0, -1);
        return objectList.stream().map(String::valueOf).collect(Collectors.toList());
    }
}


