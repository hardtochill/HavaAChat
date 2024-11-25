package cn.havaachat.service.impl;

import cn.havaachat.constants.ContactConstants;
import cn.havaachat.context.BaseContext;
import cn.havaachat.enums.*;
import cn.havaachat.exception.BaseException;
import cn.havaachat.mapper.GroupInfoMapper;
import cn.havaachat.mapper.UserContactApplyMapper;
import cn.havaachat.mapper.UserContactMapper;
import cn.havaachat.mapper.UserInfoMapper;
import cn.havaachat.pojo.dto.*;
import cn.havaachat.pojo.entity.GroupInfo;
import cn.havaachat.pojo.entity.UserContact;
import cn.havaachat.pojo.entity.UserContactApply;
import cn.havaachat.pojo.entity.UserInfo;
import cn.havaachat.pojo.vo.PageResultVO;
import cn.havaachat.redis.RedisService;
import cn.havaachat.service.UserContactApplyService;
import cn.havaachat.service.UserContactService;
import cn.havaachat.utils.StringUtils;
import cn.havaachat.websocket.MessageHandler;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserContactApplyServiceImpl implements UserContactApplyService {
    private UserContactApplyMapper userContactApplyMapper;
    private UserContactMapper userContactMapper;
    private UserInfoMapper userInfoMapper;
    private GroupInfoMapper groupInfoMapper;
    private UserContactService userContactService;
    private MessageHandler messageHandler;
    public UserContactApplyServiceImpl(UserContactApplyMapper userContactApplyMapper,UserContactMapper userContactMapper,UserInfoMapper userInfoMapper
            ,GroupInfoMapper groupInfoMapper,UserContactService userContactService,MessageHandler messageHandler){
        this.userContactApplyMapper = userContactApplyMapper;
        this.userContactMapper = userContactMapper;
        this.userInfoMapper=userInfoMapper;
        this.groupInfoMapper = groupInfoMapper;
        this.userContactService = userContactService;
        this.messageHandler = messageHandler;
    }
    /**
     * 申请添加联系人
     * @param contactApplyAddDTO
     * @return 返回添加类型
     */
    @Override
    @Transactional
    public Integer applyAdd(ContactApplyAddDTO contactApplyAddDTO) {
        log.info("申请添加联系人：{}",contactApplyAddDTO);
        // 判断要添加的联系人是用户还是群聊
        UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getById(contactApplyAddDTO.getContactId());
        if(null==userContactTypeEnum){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        // 取出用户信息
        TokenUserInfoDTO tokenUserInfo = BaseContext.getTokenUserInfo();
        // 申请人id
        String applyUserId = tokenUserInfo.getUserId();
        // 填充默认申请信息
        String applyInfo = contactApplyAddDTO.getApplyInfo();
        applyInfo = StringUtils.isEmpty(applyInfo)?String.format(ContactConstants.APPLY_INFO_TEMPLATE,tokenUserInfo.getNickName()):applyInfo;
        // 被申请人id
        String receiveUserId = contactApplyAddDTO.getContactId();
        // 校验 “被申请人” 是否已将向 “申请人” 发送了好友请求，即A向B申请添加好友前，校验B是否已经向A发送了好友申请且未处理
        UserContactApply existReceiverUserAlreadyApply = userContactApplyMapper.findByApplyUserIdAndReceiveUserIdAndContactId(receiveUserId, applyUserId, applyUserId);
        if(null!=existReceiverUserAlreadyApply && UserContactApplyStatusEnum.INIT.getStatus().equals(existReceiverUserAlreadyApply.getStatus())){
            throw new BaseException(ResponseCodeEnum.CODE_400.getCode(),"对方已向你发送好友申请，请先处理");
        }
        // 校验 “申请人”是否已被“被申请人”拉黑——我想加你，但我已经被你拉黑了
        UserContact userContactForApplyBeBlacked = userContactMapper.findByUserIdAndContactId(applyUserId, receiveUserId);
        if(null!=userContactForApplyBeBlacked &&
                ArrayUtils.contains(
                        new Integer[]{UserContactStatusEnum.BLACKLIST_BE.getStatus(),UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus()}
                        ,userContactForApplyBeBlacked.getStatus())){
            throw new BaseException(ResponseCodeEnum.CODE_400.getCode(),"对方已将你拉黑，无法添加");
        }
        // 校验“被申请人”是否已被“申请人”拉黑——我想加你，但你已经被我拉黑了
        UserContact userContactForReceiverBeBlacked = userContactMapper.findByUserIdAndContactId(receiveUserId, applyUserId);
        if(null!=userContactForReceiverBeBlacked &&
                ArrayUtils.contains(
                        new Integer[]{UserContactStatusEnum.BLACKLIST_BE.getStatus(),UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus()}
                        ,userContactForReceiverBeBlacked.getStatus())){
            throw new BaseException(ResponseCodeEnum.CODE_400.getCode(),"你已将对方拉黑，无法添加");
        }
        // 添加类型
        Integer joinType = null;
        switch (userContactTypeEnum){
            case USER:
                UserInfo receiveUser = userInfoMapper.findById(receiveUserId);
                if(null==receiveUser){
                    throw new BaseException(ResponseCodeEnum.CODE_600);
                }
                joinType = receiveUser.getJoinType();
                break;
            case GROUP:
                GroupInfo receiveGroup = groupInfoMapper.findById(receiveUserId);
                if(null==receiveGroup || GroupStatusEnum.DISSOLUTION.getStatus().equals(receiveGroup.getStatus())){
                    throw new BaseException(ResponseCodeEnum.CODE_400.getCode(),"群聊不存在或已解散");
                }
                // 申请添加群，则被申请人应该是群主
                receiveUserId = receiveGroup.getGroupOwnerId();
                joinType = receiveGroup.getJoinType();
                break;
        }
        // 如果joinType是直接添加，则不用存储申请记录
        if(JoinTypeEnum.JOIN.getType().equals(joinType)){
            userContactService.addContact(applyUserId,receiveUserId,contactApplyAddDTO.getContactId(),userContactTypeEnum.getType(),applyInfo);
            return joinType;
        }
        // 如果joinType是申请后才能加，则需要存储申请记录
        UserContactApply  existUserContactApply = userContactApplyMapper.findByApplyUserIdAndReceiveUserIdAndContactId(applyUserId,receiveUserId, contactApplyAddDTO.getContactId());
        Long now = System.currentTimeMillis();
        // 是否为初次申请
        if(null==existUserContactApply){
            UserContactApply userContactApply = new UserContactApply();
            userContactApply.setApplyUserId(applyUserId);
            userContactApply.setReceiveUserId(receiveUserId);
            userContactApply.setContactId(contactApplyAddDTO.getContactId());
            userContactApply.setContactType(userContactTypeEnum.getType());
            userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            userContactApply.setApplyInfo(applyInfo);
            userContactApply.setLastApplyTime(now);
            userContactApplyMapper.insert(userContactApply);
        }else{
            UserContactApply userContactApply = new UserContactApply();
            userContactApply.setApplyId(existUserContactApply.getApplyId());
            userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            userContactApply.setApplyInfo(applyInfo);
            userContactApply.setLastApplyTime(now);
            userContactApplyMapper.update(userContactApply);
        }
        // 给被申请用户发送ws信息，只有当初次申请 或者 被拒绝或拉黑后再次申请，才向被申请者发送ws消息。如果是已申请但未处理，再次申请时就不再发送ws消息
        if(null==existUserContactApply || !UserContactApplyStatusEnum.INIT.getStatus().equals(existUserContactApply.getStatus())){
            MessageSendDTO messageSendDTO = new MessageSendDTO();
            messageSendDTO.setMessageType(MessageTypeEnum.CONTACT_APPLY.getType());
            messageSendDTO.setMessageContent(applyInfo);
            messageSendDTO.setContactId(receiveUserId);
            messageHandler.sendMessage(messageSendDTO);
        }
        return joinType;
    }
    /**
     * 分页获取申请列表
     * @param pageDTO
     * @return
     */
    @Override
    public PageResultVO loadApply(PageDTO pageDTO) {
        log.info("分页加载好友申请列表：{}",pageDTO);
        // 当前查询用户即为被申请人
        String receiveUserId = BaseContext.getTokenUserInfo().getUserId();
        PageHelper.startPage(pageDTO.getPageNo(), pageDTO.getPageSize());
        Page<UserContactApply> pageResult = userContactApplyMapper.findBatchWithContactNameByReceiveUserId(receiveUserId);
        return new PageResultVO(pageDTO.getPageNo(), pageDTO.getPageSize(), pageResult.getTotal(),pageResult.getResult());
    }
    /**
     * 处理好友申请
     * @param applyId
     * @param status
     */
    @Transactional
    @Override
    public void dealWithApply(Integer applyId,Integer status){
        log.info("处理好友申请：applyId={}，status={}",applyId,status);
        String receiveUserId = BaseContext.getTokenUserInfo().getUserId();
        // 从前端提交过来的状态是用户点击选择后的结果，因此一定不为“待处理”
        UserContactApplyStatusEnum userContactApplyStatusEnum = UserContactApplyStatusEnum.getByStatus(status);
        if(null==userContactApplyStatusEnum || UserContactApplyStatusEnum.INIT.equals(userContactApplyStatusEnum)){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        // 当前用户只能处理属于自己的申请
        UserContactApply userContactApply = userContactApplyMapper.findByApplyId(applyId);
        if(null==userContactApply || !userContactApply.getReceiveUserId().equals(receiveUserId)){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        // 申请状态修改是一个单向流操作，即只能修改状态初始值为“待处理”的申请
        Integer changedRow = userContactApplyMapper.updateStatusAndUpdateTimeByApplyIdAndStatus(applyId, UserContactApplyStatusEnum.INIT.getStatus(), status, LocalDateTime.now());
        if(0==changedRow){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }

        // 1.同意添加
        if(UserContactApplyStatusEnum.PASS.equals(userContactApplyStatusEnum)){
            userContactService.addContact(userContactApply.getApplyUserId(),userContactApply.getReceiveUserId(),userContactApply.getContactId(),userContactApply.getContactType(),userContactApply.getApplyInfo());
            return;
        }

        // 2.拒绝 不用处理

        // 3.拉黑
        if(UserContactApplyStatusEnum.BLACKLIST.equals(userContactApplyStatusEnum)){
            // 修改或插入联系状态
            UserContact userContact = new UserContact();
            userContact.setUserId(userContactApply.getApplyUserId());
            // 这里如果群主代表群拉黑用户，则拉黑关系是：用户被群聊拉黑，不是用户被群主拉黑，用户还是能添加群主，但不能添加群
            userContact.setContactId(userContactApply.getContactId());
            userContact.setContactType(userContactApply.getContactType());
            userContact.setStatus(UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus());
            // 如果存在已存在联系关系记录，则修改状态为还未添加好友就被拉黑；如果不存在联系关系，则插入，防止申请人在被拉黑后还能添加被申请人
            UserContact existUserContact = userContactMapper.findByUserIdAndContactId(userContact.getUserId(), userContact.getContactId());
            if(null!=existUserContact){
                userContactMapper.updateByUserIdAndContactId(userContact);
            }else{
                userContactMapper.insert(userContact);
            }
        }
    }
}
