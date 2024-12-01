package cn.havaachat.controller.admin;

import cn.havaachat.annotation.GlobalInterceptor;
import cn.havaachat.annotation.PageQueryAutoFill;
import cn.havaachat.pojo.dto.PageDTO;
import cn.havaachat.pojo.vo.PageResultVO;
import cn.havaachat.pojo.vo.ResponseVO;
import cn.havaachat.service.AdminService;
import cn.havaachat.service.GroupInfoService;
import cn.havaachat.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 后台管理：群聊
 */
@RestController
@RequestMapping("/admin")
@Validated
public class AdminGroupInfoController {
    private AdminService adminService;
    private GroupInfoService groupInfoService;
    @Autowired
    public AdminGroupInfoController(AdminService adminService,GroupInfoService groupInfoService){
        this.adminService = adminService;
        this.groupInfoService = groupInfoService;
    }
    /**
     * 获取群组列表
     * @param pageDTO
     * @param groupId
     * @param groupNameFuzzy
     * @param groupOwnerId
     * @return
     */
    @PostMapping("/loadGroup")
    @GlobalInterceptor(checkAdmin = true)
    @PageQueryAutoFill
    public ResponseVO<PageResultVO> loadGroup(@Valid PageDTO pageDTO, String groupId, String groupNameFuzzy, String groupOwnerId){
        PageResultVO pageResultVO = groupInfoService.loadGroup(pageDTO,groupId,groupNameFuzzy,groupOwnerId);
        return ResponseUtils.success(pageResultVO);
    }

    /**
     * 解散群组
     * @param groupId
     * @return
     */
    @PostMapping("/dissolutionGroup")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO dissolutionGroup(@NotNull String groupId){
        groupInfoService.dissolutionGroup(groupId);
        return ResponseUtils.success();
    }
}
