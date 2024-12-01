package cn.havaachat.service.impl;

import cn.havaachat.config.AppConfiguration;
import cn.havaachat.constants.AccountConstants;
import cn.havaachat.enums.*;
import cn.havaachat.exception.BaseException;
import cn.havaachat.mapper.*;
import cn.havaachat.pojo.dto.AppUpdateDTO;
import cn.havaachat.pojo.dto.PageDTO;
import cn.havaachat.pojo.dto.SysSettingDTO;
import cn.havaachat.pojo.entity.AppUpdate;
import cn.havaachat.pojo.entity.GroupInfo;
import cn.havaachat.pojo.entity.UserInfo;
import cn.havaachat.pojo.entity.UserInfoBeauty;
import cn.havaachat.pojo.vo.PageResultVO;
import cn.havaachat.redis.RedisService;
import cn.havaachat.service.AdminService;
import cn.havaachat.utils.FilePathUtils;
import cn.havaachat.utils.StringUtils;
import cn.havaachat.websocket.ChannelContextUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台管理
 */
@Service
@Slf4j
public class AdminServiceImpl implements AdminService {
    private UserInfoMapper userInfoMapper;
    private GroupInfoMapper groupInfoMapper;
    private UserInfoBeautyMapper userInfoBeautyMapper;
    private UserContactMapper userContactMapper;
    private RedisService redisService;
    private AppConfiguration appConfiguration;
    private ChannelContextUtils channelContextUtils;
    @Autowired
    public AdminServiceImpl(UserInfoMapper userInfoMapper,GroupInfoMapper groupInfoMapper,UserInfoBeautyMapper userInfoBeautyMapper
            ,UserContactMapper userContactMapper,RedisService redisService,AppConfiguration appConfiguration
            ,ChannelContextUtils channelContextUtils){
        this.userInfoMapper=userInfoMapper;
        this.groupInfoMapper = groupInfoMapper;
        this.userInfoBeautyMapper=userInfoBeautyMapper;
        this.userContactMapper = userContactMapper;
        this.redisService = redisService;
        this.appConfiguration = appConfiguration;
        this.channelContextUtils = channelContextUtils;
    }

    /**
     * 加载用户列表
     * @param pageDTO
     * @param userId
     * @param nickNameFuzzy
     * @return
     */
    @Override
    public PageResultVO loadUser(PageDTO pageDTO,String userId,String nickNameFuzzy) {
        log.info("管理后台：分页查询用户列表：{}，userId：{}，nickNameFuzzy：{}",pageDTO,userId,nickNameFuzzy);
        UserInfo userInfoForQuery = new UserInfo();
        userInfoForQuery.setUserId(StringUtils.isEmpty(userId)?null:userId);
        userInfoForQuery.setNickName(StringUtils.isEmpty(nickNameFuzzy)?null:nickNameFuzzy);
        PageHelper.startPage(pageDTO.getPageNo(), pageDTO.getPageSize());
        Page<UserInfo> userInfoPage = userInfoMapper.findBatch(userInfoForQuery);
        return new PageResultVO(pageDTO.getPageNo(), pageDTO.getPageSize(), userInfoPage.getTotal(), userInfoPage.getResult());
    }
    /**
     * 更改用户状态：启用or禁用
     * @param status
     * @param userId
     */
    @Override
    public void updateUserStatus(Integer status, String userId){
       log.info("管理后台：更改用户状态，status：{}，userId：{}",status,userId);
        UserStatusEnum userStatusEnum = UserStatusEnum.getByStatus(status);
        if(null==userStatusEnum){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setStatus(status);
        userInfoMapper.update(userInfo);
    }

    /**
     * 强制下线
     * @param userId
     */
    @Override
    public void forceOffLine(String userId) {
        log.info("管理后台：强制下线：userId：{}",userId);
        channelContextUtils.closeContext(userId);
    }
    /**
     * 获取靓号列表
     * @param pageDTO
     * @param userIdFuzzy
     * @param emailFuzzy
     * @return
     */
    @Override
    public PageResultVO loadBeautyAccountList(PageDTO pageDTO,String userIdFuzzy,String emailFuzzy) {
        log.info("管理后台：分页查询靓号列表：{}，userIdFuzzy：{}，emailFuzzy：{}",pageDTO,userIdFuzzy,emailFuzzy);
        PageHelper.startPage(pageDTO.getPageNo(), pageDTO.getPageSize());
        UserInfoBeauty userInfoBeauty = new UserInfoBeauty();
        userInfoBeauty.setUserId(StringUtils.isEmpty(userIdFuzzy)?null:userIdFuzzy);
        userInfoBeauty.setEmail(StringUtils.isEmpty(emailFuzzy)?null:emailFuzzy);
        Page<UserInfoBeauty> page = userInfoBeautyMapper.findBatch(userInfoBeauty);
        return new PageResultVO(pageDTO.getPageNo(), pageDTO.getPageSize(), page.getTotal(), page.getResult());
    }
    /**
     * 新增或修改靓号
     * @param userInfoBeauty
     */
    @Override
    public void saveBeautyAccount(UserInfoBeauty userInfoBeauty) {
       log.info("管理后台：新增或修改靓号：{}",userInfoBeauty);
        // 为了应对绕过前端的攻击：处于使用状态的靓号无法修改
        UserInfoBeauty existUserInfoBeautyById = userInfoBeautyMapper.findById(userInfoBeauty.getId());
        if(existUserInfoBeautyById!=null && BeautyAccountStatusEnum.USED.getStatus().equals(existUserInfoBeautyById.getStatus())){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        UserInfoBeauty existUserInfoBeautyByEmail = userInfoBeautyMapper.findByEmail(userInfoBeauty.getEmail());
        UserInfoBeauty existUserInfoBeautyByUserId = userInfoBeautyMapper.findByUserId(userInfoBeauty.getUserId());
        if(null==userInfoBeauty.getId()){ // 新增
            // 新增靓号的邮箱是否已被使用
            if(null!=existUserInfoBeautyByEmail){
                throw new BaseException(ResponseCodeEnum.CODE_400.getCode(),"当前邮箱已存在靓号");
            }
            // 新增靓号的号码是否已被使用
            if(null!=existUserInfoBeautyByUserId){
                throw new BaseException(ResponseCodeEnum.CODE_400.getCode(),"当前靓号已存在");
            }
        }else{ // 修改
            // 要修改的邮箱是否已被使用
            // 如果 null!=existUserInfoBeautyByEmail && existUserInfoBeautyByEmail.getId().equals(userInfoBeauty.getId()) == true
            // 说明本次要修改的内容不包括邮箱，邮箱只是顺带被传过来了
            // 如果 null!=existUserInfoBeautyByEmail && !existUserInfoBeautyByEmail.getId().equals(userInfoBeauty.getId()) == true
            // 说明本次要修改的内容包括邮箱，并且要修改成的邮箱已经被使用
            if(null!=existUserInfoBeautyByEmail && !existUserInfoBeautyByEmail.getId().equals(userInfoBeauty.getId())){
                throw new BaseException(ResponseCodeEnum.CODE_400.getCode(),"当前邮箱已存在靓号");
            }
            // 要修改的靓号是否已被使用
            if(null!=existUserInfoBeautyByUserId && !existUserInfoBeautyByUserId.getId().equals(userInfoBeauty.getId())){
                throw new BaseException(ResponseCodeEnum.CODE_400.getCode(),"当前靓号已存在");
            }
        }
        // 要新增或修改的靓号的userId和email，必须保证未被注册
        // 对于普通注册的邮箱和账号，是不会存入靓号表的；因此当要修改或新增一个靓号时，有必要判断靓号要使用的邮箱和账号是否已经被使用
        UserInfo existUserInfoByEmail = userInfoMapper.findByEmail(userInfoBeauty.getEmail());
        if(null!=existUserInfoByEmail){
            throw new BaseException(ResponseCodeEnum.CODE_400.getCode(),"当前邮箱已经被注册");
        }
        UserInfo existUserInfoByUserId = userInfoMapper.findById(StringUtils.spliceUserId(userInfoBeauty.getUserId()));
        if(null!=existUserInfoByUserId){
            throw new BaseException(ResponseCodeEnum.CODE_400.getCode(),"当前靓号已经被注册");
        }
        if(null==userInfoBeauty.getId()){ // 新增
            userInfoBeauty.setStatus(BeautyAccountStatusEnum.NO_USE.getStatus());
            userInfoBeautyMapper.insert(userInfoBeauty);
        }else{ // 修改
            userInfoBeautyMapper.update(userInfoBeauty);
        }
    }

    /**
     * 删除靓号
     * @param id
     */
    @Override
    public void deleteBeautyAccount(Integer id) {
        log.info("管理后台：删除靓号，id：{}",id);
        userInfoBeautyMapper.deleteById(id);
    }

    /**
     * 获取群组列表
     * @param pageDTO
     * @param groupId
     * @param groupNameFuzzy
     * @param groupOwnerId
     * @return
     */
    @Override
    public PageResultVO loadGroup(PageDTO pageDTO,String groupId,String groupNameFuzzy,String groupOwnerId) {
        log.info("管理后台：分页查询群组列表：{}",pageDTO);
        PageHelper.startPage(pageDTO.getPageNo(),pageDTO.getPageSize());
        GroupInfo groupInfoForQuery = new GroupInfo();
        groupInfoForQuery.setGroupId(StringUtils.isEmpty(groupId)?null:groupId);
        groupInfoForQuery.setGroupName(StringUtils.isEmpty(groupNameFuzzy)?null:groupNameFuzzy);
        groupInfoForQuery.setGroupOwnerId(StringUtils.isEmpty(groupOwnerId)?null:groupOwnerId);
        Page<GroupInfo> page = groupInfoMapper.findBatch(groupInfoForQuery);
        return new PageResultVO(pageDTO.getPageNo(), pageDTO.getPageSize(), page.getTotal(),page.getResult());
    }

    /**
     * 解散群组
     * @param groupId
     */
    @Override
    public void dissolutionGroup(String groupId) {
        log.info("管理后台：解散群组：groupId：{}",groupId);
        GroupInfo existGroupInfo = groupInfoMapper.findById(groupId);
        if(null==existGroupInfo){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        // 更新群组状态
        GroupInfo groupInfoForUpdate = new GroupInfo();
        groupInfoForUpdate.setGroupId(groupId);
        groupInfoForUpdate.setStatus(GroupStatusEnum.DISSOLUTION.getStatus());
        groupInfoMapper.update(groupInfoForUpdate);
        // 更新与群组相关的所有联系
        userContactMapper.updateStatusByContactId(UserContactStatusEnum.DEL.getStatus(),groupId);
        // todo 移除相关群员的联系人缓存
        // todo 发消息：1.更新会话信息，2.记录群消息，3.发送解散通知
    }
    /**
     * 保存系统设置
     * @param sysSettingDTO
     * @param robotFile
     * @param robotCover
     */
    @Override
    public void saveSysSetting(SysSettingDTO sysSettingDTO, MultipartFile robotFile, MultipartFile robotCover) {
        log.info("管理后台：保存系统设置，sysSettingDTO：{}",sysSettingDTO);
        if(null!=robotFile){
            String avatarFileFolderPath = FilePathUtils.generateAvatarFileFolderPath(appConfiguration.getFileFolder());
            File avatarFileFolder = new File(avatarFileFolderPath);
            if(!avatarFileFolder.exists()){
                avatarFileFolder.mkdirs();
            }
            try{
                String avatarFilePath = FilePathUtils.generateAvatarFilePath(avatarFileFolderPath, AccountConstants.ROBOT_UID);
                String coverAvatarFilePath = FilePathUtils.generateCoverAvatarFilePath(avatarFileFolderPath, AccountConstants.ROBOT_UID);
                robotFile.transferTo(new File(avatarFilePath));
                robotCover.transferTo(new File(coverAvatarFilePath));
            }catch (Exception e){
                log.error("管理后台：将系统设置头像文件存储至本地失败");
                throw new BaseException(ResponseCodeEnum.CODE_500);
            }
        }
        redisService.saveSysSetting(sysSettingDTO);
    }
}
