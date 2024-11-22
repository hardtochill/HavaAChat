package cn.havaachat.controller.user;

import cn.havaachat.annotation.GlobalInterceptor;
import cn.havaachat.annotation.PageQueryAutoFill;
import cn.havaachat.enums.UserContactStatusEnum;
import cn.havaachat.pojo.dto.ContactApplyAddDTO;
import cn.havaachat.pojo.dto.PageDTO;
import cn.havaachat.pojo.vo.*;
import cn.havaachat.service.UserContactApplyService;
import cn.havaachat.service.UserContactService;
import cn.havaachat.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/contact")
@Slf4j
@Validated
public class UserContactController {
    private UserContactService userContactService;
    private UserContactApplyService userContactApplyService;
    @Autowired
    public UserContactController(UserContactService userContactService,UserContactApplyService userContactApplyService){
        this.userContactService = userContactService;
        this.userContactApplyService = userContactApplyService;
    }

    /**
     * 搜索联系人
     * @param contactId
     * @return
     */
    @PostMapping("/search")
    @GlobalInterceptor
    public ResponseVO<UserContactSearchResultVO> search(@NotEmpty String contactId){
        UserContactSearchResultVO userContactSearchResultVO = userContactService.search(contactId);
        return ResponseUtils.success(userContactSearchResultVO);
    }

    /**
     * 申请添加好友
     * @param contactApplyAddDTO
     * @return
     */
    @PostMapping("/applyAdd")
    @GlobalInterceptor
    public ResponseVO<Integer> applyAdd(@Valid ContactApplyAddDTO contactApplyAddDTO){
        Integer joinType = userContactApplyService.applyAdd(contactApplyAddDTO);
        return ResponseUtils.success(joinType);
    }

    /**
     * 加载申请列表
     * @param pageDTO
     * @return
     */
    @PostMapping("/loadApply")
    @GlobalInterceptor
    @PageQueryAutoFill
    public ResponseVO<PageResultVO> loadApply(@Valid PageDTO pageDTO){
        PageResultVO pageResultVO = userContactApplyService.loadApply(pageDTO);
        return ResponseUtils.success(pageResultVO);
    }

    /**
     * 处理申请
     * @param applyId
     * @param status
     * @return
     */
    @PostMapping("/dealWithApply")
    @GlobalInterceptor
    public ResponseVO dealWithApply(@NotNull Integer applyId,@NotNull Integer status){
        userContactApplyService.dealWithApply(applyId,status);
        return ResponseUtils.success();
    }

    /**
     * 加载联系人列表
     * @param contactType
     * @return
     */
    @PostMapping("/loadContact")
    @GlobalInterceptor
    public ResponseVO<List<UserContactLoadResultVO>> loadContact(@NotNull String contactType){
        List<UserContactLoadResultVO> userContactLoadResultVOList = userContactService.loadContact(contactType);
        return ResponseUtils.success(userContactLoadResultVOList);
    }

    /**
     * 获取目标用户信息（从好友列表或从群成员），目标用户可以是好友也可以是非好友
     * @param contactId
     * @return
     */
    @PostMapping("/getContactInfo")
    @GlobalInterceptor
    public ResponseVO<UserInfoVO> getContactInfoForAll(@NotNull String contactId){
        UserInfoVO targetUserInfoVO = userContactService.getContactInfoForAll(contactId);
        return ResponseUtils.success(targetUserInfoVO);
    }

    /**
     * 获取目标用户信息（从好友列表），目标用户只能是好友
     * @param contactId
     * @return
     */
    @PostMapping("/getContactUserInfo")
    @GlobalInterceptor
    public ResponseVO<UserInfoVO> getContactInfoOnlyFriend(@NotNull String contactId){
        UserInfoVO targetUserInfoVO = userContactService.getContactInfoOnlyFriend(contactId);
        return ResponseUtils.success(targetUserInfoVO);
    }

    /**
     * 删除联系人
     * @param contactId
     * @return
     */
    @PostMapping("/delContact")
    @GlobalInterceptor
    public ResponseVO addContactToDeleteList(@NotNull String contactId){
        userContactService.addContactToDeleteOrBlackList(contactId, UserContactStatusEnum.DEL);
        return ResponseUtils.success();
    }
    /**
     * 拉黑联系人
     * @param contactId
     * @return
     */
    @PostMapping("/addContact2BlackList")
    @GlobalInterceptor
    public ResponseVO addContactToBlackList(@NotNull String contactId){
        userContactService.addContactToDeleteOrBlackList(contactId, UserContactStatusEnum.BLACKLIST);
        return ResponseUtils.success();
    }
}
