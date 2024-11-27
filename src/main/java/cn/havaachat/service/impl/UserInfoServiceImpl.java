package cn.havaachat.service.impl;

import cn.havaachat.config.AppConfiguration;
import cn.havaachat.context.BaseContext;
import cn.havaachat.mapper.ChatSessionUserMapper;
import cn.havaachat.mapper.UserContactMapper;
import cn.havaachat.mapper.UserInfoMapper;
import cn.havaachat.pojo.dto.SaveUserInfoDTO;
import cn.havaachat.pojo.dto.TokenUserInfoDTO;
import cn.havaachat.pojo.entity.UserInfo;
import cn.havaachat.pojo.vo.UserInfoVO;
import cn.havaachat.redis.RedisService;
import cn.havaachat.service.ChatSessionUserService;
import cn.havaachat.service.UserInfoService;
import cn.havaachat.utils.FilePathUtils;
import cn.havaachat.utils.StringUtils;
import cn.havaachat.websocket.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class UserInfoServiceImpl implements UserInfoService {
    private UserInfoMapper userInfoMapper;
    private AppConfiguration appConfiguration;
    private ChatSessionUserMapper chatSessionUserMapper;
    private MessageHandler messageHandler;
    private UserContactMapper userContactMapper;
    private ChatSessionUserService chatSessionUserService;
    private RedisService redisService;
    @Autowired
    public UserInfoServiceImpl(UserInfoMapper userInfoMapper,AppConfiguration appConfiguration,ChatSessionUserMapper chatSessionUserMapper,
                               MessageHandler messageHandler,UserContactMapper userContactMapper,ChatSessionUserService chatSessionUserService,
                               RedisService redisService){
        this.userInfoMapper=userInfoMapper;
        this.appConfiguration = appConfiguration;
        this.chatSessionUserMapper = chatSessionUserMapper;
        this.messageHandler = messageHandler;
        this.userContactMapper = userContactMapper;
        this.chatSessionUserService = chatSessionUserService;
        this.redisService = redisService;
    }
    /**
     * 获取用户信息
     * @return
     */
    @Override
    public UserInfoVO getUserInfo() {
        TokenUserInfoDTO tokenUserInfoDTO = BaseContext.getTokenUserInfo();
        String userId = tokenUserInfoDTO.getUserId();
        log.info("获取用户信息：{}",userId);
        UserInfo userInfo = userInfoMapper.findById(userId);
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(userInfo,userInfoVO);
        userInfoVO.setAdmin(tokenUserInfoDTO.getAdmin());
        return userInfoVO;
    }
    /**
     * 保存用户信息
     * @param saveUserInfoDTO
     */
    @Override
    @Transactional
    public void saveUserInfo(SaveUserInfoDTO saveUserInfoDTO) throws IOException {
        String userId = BaseContext.getTokenUserInfo().getUserId();
        log.info("保存用户信息：userId：{}，saveUserInfoDTO：{}",userId,saveUserInfoDTO);
        // 覆盖本地保存的头像信息
        if(null!=saveUserInfoDTO.getAvatarFile()){
            String avatarFileFolderPath = FilePathUtils.generateAvatarFileFolderPath(appConfiguration.getFileFolder());
            File avatarFileFolder = new File(avatarFileFolderPath);
            if(!avatarFileFolder.exists()){
                avatarFileFolder.mkdirs();
            }
            String avatarFilePath = FilePathUtils.generateAvatarFilePath(avatarFileFolderPath, userId);
            String coverAvatarFilePath = FilePathUtils.generateCoverAvatarFilePath(avatarFileFolderPath, userId);
            saveUserInfoDTO.getAvatarFile().transferTo(new File(avatarFilePath));
            saveUserInfoDTO.getAvatarCover().transferTo(new File(coverAvatarFilePath));
        }
        // 修改数据库中的用户信息
        UserInfo updateUserInfo = new UserInfo();
        BeanUtils.copyProperties(saveUserInfoDTO,updateUserInfo);
        updateUserInfo.setUserId(userId);
        // 该接口不提供修改密码功能，为防止有人绕过前端攻击，需额外把密码置null
        updateUserInfo.setPassword(null);

        UserInfo originUserInfo = userInfoMapper.findById(userId);

        userInfoMapper.update(updateUserInfo);

        // 判断本次修改是否涉及到名称的修改，若涉及名称修改，则还要更新redis和会话中的昵称信息
        if(!originUserInfo.getNickName().equals(updateUserInfo.getNickName())) {
            // 更新redis中的tokenUserInfoDTO
            TokenUserInfoDTO tokenUserInfoDTO = redisService.getTokenUserInfoDTOByUserId(userId);
            tokenUserInfoDTO.setNickName(updateUserInfo.getNickName());
            redisService.saveTokenUserInfoDTO(tokenUserInfoDTO);
            // 更新该用户所有好友的会话中的昵称信息
            chatSessionUserService.updateChatSessionUserName(userId,updateUserInfo.getNickName());
        }
    }

    /**
     * 修改密码
     * @param password
     */
    @Override
    public void updatePassword(String password) {
        String userId = BaseContext.getTokenUserInfo().getUserId();
        log.info("修改密码：userId：{}",userId);
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setPassword(StringUtils.transferStringToMd5(password));
        userInfoMapper.update(userInfo);
        // todo 强制退出，重新登陆
    }

}
