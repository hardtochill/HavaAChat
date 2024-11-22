package cn.havaachat.service.impl;

import cn.havaachat.config.AppConfiguration;
import cn.havaachat.context.BaseContext;
import cn.havaachat.enums.GroupStatusEnum;
import cn.havaachat.enums.ResponseCodeEnum;
import cn.havaachat.enums.UserContactStatusEnum;
import cn.havaachat.enums.UserContactTypeEnum;
import cn.havaachat.exception.BaseException;
import cn.havaachat.mapper.GroupInfoMapper;
import cn.havaachat.mapper.UserContactMapper;
import cn.havaachat.pojo.dto.SaveGroupDTO;
import cn.havaachat.pojo.dto.SysSettingDTO;
import cn.havaachat.pojo.dto.TokenUserInfoDTO;
import cn.havaachat.pojo.entity.GroupInfo;
import cn.havaachat.pojo.entity.UserContact;
import cn.havaachat.pojo.vo.GroupInfoVO;
import cn.havaachat.redis.RedisService;
import cn.havaachat.service.GroupInfoService;
import cn.havaachat.utils.FilePathUtils;
import cn.havaachat.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class GroupInfoServiceImpl implements GroupInfoService {
    private GroupInfoMapper groupInfoMapper;
    private RedisService redisService;
    private UserContactMapper userContactMapper;
    private AppConfiguration appConfiguration;
    @Autowired
    public GroupInfoServiceImpl(GroupInfoMapper groupInfoMapper,RedisService redisService,UserContactMapper userContactMapper,
                                AppConfiguration appConfiguration){
        this.groupInfoMapper = groupInfoMapper;
        this.redisService = redisService;
        this.userContactMapper = userContactMapper;
        this.appConfiguration = appConfiguration;
    }
    /**
     * 新增或修改群组
     * @param saveGroupDTO
     */
    @Transactional
    public void saveGroupInfo(SaveGroupDTO saveGroupDTO) throws IOException {
        // 取出存入线程上下文的用户Token
        TokenUserInfoDTO tokenUserInfoDTO = BaseContext.getTokenUserInfo();
        GroupInfo groupInfo = new GroupInfo();
        BeanUtils.copyProperties(saveGroupDTO,groupInfo);
        groupInfo.setGroupOwnerId(tokenUserInfoDTO.getUserId());
        // 新增群组
        if(StringUtils.isEmpty(groupInfo.getGroupId())){
            log.info("新增群组：{}",saveGroupDTO);
            // 判断创建者是否已达单人创建最大群组数
            Integer groupCount = groupInfoMapper.countByGroupOwnerId(groupInfo.getGroupOwnerId());
            SysSettingDTO sysSetting = redisService.getSysSetting();
            if(groupCount>=sysSetting.getMaxGroupCount()){
                throw new BaseException(ResponseCodeEnum.CODE_400.getCode(),"最多只能创建"+sysSetting.getMaxGroupCount()+"个群聊");
            }
            // 校验群头像是否为空
            if(null == saveGroupDTO.getAvatarFile()){
                throw new BaseException(ResponseCodeEnum.CODE_600);
            }
            // 生成群组id
            groupInfo.setGroupId(StringUtils.generateRandomGroupId());
            groupInfo.setStatus(GroupStatusEnum.NORMAL.getStatus());
            groupInfoMapper.insert(groupInfo);
            // 把新创建的群组加入当前用户的联系人中
            UserContact userContact = new UserContact();
            userContact.setUserId(groupInfo.getGroupOwnerId());
            userContact.setContactId(groupInfo.getGroupId());
            userContact.setContactType(UserContactTypeEnum.GROUP.getType());
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            userContactMapper.insert(userContact);

            // todo 创建会话
            // todo 发送消息
        }else{ // 修改群组
            log.info("修改群组：{}",saveGroupDTO);
            GroupInfo originGroupInfo = groupInfoMapper.findById(groupInfo.getGroupId());
            // 当前申请修改群信息的用户 不是 该群的群主
            if(!groupInfo.getGroupOwnerId().equals(originGroupInfo.getGroupOwnerId())){
                throw new BaseException(ResponseCodeEnum.CODE_600);
            }
            groupInfoMapper.update(groupInfo);
            // todo 更新相关表冗余信息
            // todo 修改群昵称，发送ws消息
        }
        if(null == saveGroupDTO.getAvatarFile()){
            return;
        }
        // 将头像文件存入本地
        String avatarFileFolderPath = FilePathUtils.generateAvatarFileFolderPath(appConfiguration.getFileFolder());
        // 创建头像文件所在目录
        File avatarFileFolder = new File(avatarFileFolderPath);
        if(!avatarFileFolder.exists()){
            avatarFileFolder.mkdirs();
        }
        // 创建头像文件所在路径
        String avatarFilePath = FilePathUtils.generateAvatarFilePath(avatarFileFolderPath,groupInfo.getGroupId());
        String coverAvatarFilePath = FilePathUtils.generateCoverAvatarFilePath(avatarFileFolderPath,groupInfo.getGroupId());
        // 存入本地
        saveGroupDTO.getAvatarFile().transferTo(new File(avatarFilePath));
        saveGroupDTO.getAvatarCover().transferTo(new File(coverAvatarFilePath));
    }

    /**
     * 获取用户创建的群聊
     * @param userId
     * @return
     */
    @Override
    public List<GroupInfo> loadMyGroup(String userId) {
        return groupInfoMapper.findBatchByGroupOwnerId(userId);
    }

    /**
     * 获取群聊详细信息
     * @param groupId
     * @return
     */
    @Override
    public GroupInfo getGroupInfo(String groupId) {
        log.info("查询群聊详细信息：{}",groupId);
        // 校验并获取群聊信息
        GroupInfo groupInfo = checkAndGetGroupInfoByGroupId(groupId);
        // 查询群成员数量
        Integer memberCount = userContactMapper.countByContactIdAndStatus(groupId,UserContactStatusEnum.FRIEND.getStatus());
        groupInfo.setMemberCount(memberCount);
        return groupInfo;
    }

    /**
     * 在聊天会话界面获取群聊详细信息
     * @param groupId
     * @return
     */
    @Override
    public GroupInfoVO getGroupInfo4Chat(String groupId) {
        log.info("在聊天会话界面查询群聊详细信息：{}",groupId);
        // 校验并获取群聊信息
        GroupInfo groupInfo = checkAndGetGroupInfoByGroupId(groupId);
        // 获取该群聊所有有效联系人
        List<UserContact> userContactList = userContactMapper.findBatchWithContactNameAndSexByContactIdAndStatus(groupId, UserContactStatusEnum.FRIEND.getStatus());
        GroupInfoVO groupInfoVO = new GroupInfoVO();
        groupInfoVO.setGroupInfo(groupInfo);
        groupInfoVO.setUserContactList(userContactList);
        return groupInfoVO;
    }

    /**
     * 校验用户与群聊的联系关系并获取该群聊信息
     * @param groupId
     * @return
     */
    public GroupInfo checkAndGetGroupInfoByGroupId(String groupId){
        // 取出当前用户信息
        TokenUserInfoDTO tokenUserInfo = BaseContext.getTokenUserInfo();
        UserContact userContact = userContactMapper.findByUserIdAndContactId(tokenUserInfo.getUserId(), groupId);
        // 校验当前用户是否在要查询的群聊中 和 该联系关系是否已删除或拉黑
        if(null==userContact || !userContact.getStatus().equals(UserContactStatusEnum.FRIEND.getStatus())){
            throw new BaseException(ResponseCodeEnum.CODE_400.getCode(),"你不在群聊中或群聊不存在或群聊已解散");
        }
        // 校验该群聊是否还存在
        GroupInfo groupInfo = groupInfoMapper.findById(groupId);
        if(null==groupInfo || !GroupStatusEnum.NORMAL.getStatus().equals(groupInfo.getStatus())){
            throw new BaseException(ResponseCodeEnum.CODE_400.getCode(),"群聊不存在或已解散");
        }
        return groupInfo;
    }
}
