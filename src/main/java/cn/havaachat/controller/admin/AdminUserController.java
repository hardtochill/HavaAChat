package cn.havaachat.controller.admin;

import cn.havaachat.annotation.GlobalInterceptor;
import cn.havaachat.annotation.PageQueryAutoFill;
import cn.havaachat.pojo.dto.PageDTO;
import cn.havaachat.pojo.vo.PageResultVO;
import cn.havaachat.pojo.vo.ResponseVO;
import cn.havaachat.service.AdminService;
import cn.havaachat.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 后台管理：用户
 */
@RestController
@RequestMapping("/admin")
@Validated
public class AdminUserController {
    private AdminService adminService;
    @Autowired
    public AdminUserController(AdminService adminService){
        this.adminService = adminService;
    }
    /**
     * 获取用户列表
     * @param pageDTO
     * @param userId
     * @param nickNameFuzzy
     * @return
     */
    @PostMapping("/loadUser")
    @GlobalInterceptor(checkAdmin = true)
    @PageQueryAutoFill
    public ResponseVO<PageResultVO> loadUser(@Valid PageDTO pageDTO, String userId, String nickNameFuzzy){
        PageResultVO pageResultVO = adminService.loadUser(pageDTO,userId,nickNameFuzzy);
        return ResponseUtils.success(pageResultVO);
    }

    /**
     * 更新用户状态
     * @param status
     * @param userId
     */
    @PostMapping("/updateUserStatus")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO updateUserStatus(@NotNull Integer status, @NotEmpty String userId){
        adminService.updateUserStatus(status,userId);
        return ResponseUtils.success();
    }

    /**
     * 强制下线
     * @param userId
     * @return
     */
    @PostMapping("/forceOffLine")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO forceOffLine(@NotEmpty String userId){
        adminService.forceOffLine(userId);
        return ResponseUtils.success();
    }
}
