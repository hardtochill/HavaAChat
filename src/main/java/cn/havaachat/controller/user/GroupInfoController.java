package cn.havaachat.controller.user;

import cn.havaachat.annotation.GlobalInterceptor;
import cn.havaachat.context.BaseContext;
import cn.havaachat.enums.ResponseCodeEnum;
import cn.havaachat.exception.BaseException;
import cn.havaachat.pojo.dto.SaveGroupDTO;
import cn.havaachat.pojo.entity.GroupInfo;
import cn.havaachat.pojo.vo.GroupInfoVO;
import cn.havaachat.pojo.vo.ResponseVO;
import cn.havaachat.service.GroupInfoService;
import cn.havaachat.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/group")
@Validated
public class GroupInfoController {
    private GroupInfoService groupInfoService;
    @Autowired
    public GroupInfoController(GroupInfoService groupInfoService){
        this.groupInfoService = groupInfoService;
    }

    /**
     * 新增或修改群组
     * @param saveGroupDTO
     * @return
     */
    @GlobalInterceptor
    @PostMapping("/saveGroup")
    public ResponseVO saveGroup(@Valid SaveGroupDTO saveGroupDTO) throws IOException {
        groupInfoService.saveGroupInfo(saveGroupDTO);
        return ResponseUtils.success();
    }

    /**
     * 获取用户创建的群聊
     * @return
     */
    @GlobalInterceptor
    @PostMapping("/loadMyGroup")
    public ResponseVO<List<GroupInfo>> loadMyGroup(){
        List<GroupInfo>  myGroupInfoList = groupInfoService.loadMyGroup(BaseContext.getTokenUserInfo().getUserId());
        return ResponseUtils.success(myGroupInfoList);
    }

    /**
     * 获取群聊详细信息
     * @param groupId
     * @return
     */
    @GlobalInterceptor
    @PostMapping("/getGroupInfo")
    public ResponseVO<GroupInfo> getGroupInfo(@NotEmpty String groupId){
        GroupInfo groupInfo = groupInfoService.getGroupInfo(groupId);
        return ResponseUtils.success(groupInfo);
    }

    /**
     * 在聊天会话界面获取群聊详细信息
     * @param groupId
     * @return
     */
    @GlobalInterceptor
    @PostMapping("/getGroupInfo4Chat")
    public ResponseVO<GroupInfoVO> getGroupInfo4Chat(@NotEmpty String groupId){
        GroupInfoVO groupInfoVO = groupInfoService.getGroupInfo4Chat(groupId);
        return ResponseUtils.success(groupInfoVO);
    }
}
