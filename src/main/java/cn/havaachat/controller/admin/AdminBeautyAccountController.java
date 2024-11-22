package cn.havaachat.controller.admin;

import cn.havaachat.annotation.GlobalInterceptor;
import cn.havaachat.annotation.PageQueryAutoFill;
import cn.havaachat.pojo.dto.PageDTO;
import cn.havaachat.pojo.entity.UserInfoBeauty;
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
import javax.validation.constraints.NotNull;

/**
 * 后台管理：靓号
 */
@RestController
@RequestMapping("/admin")
@Validated
public class AdminBeautyAccountController {
    private AdminService adminService;
    @Autowired
    public AdminBeautyAccountController(AdminService adminService){
        this.adminService = adminService;
    }
    /**
     * 获取靓号列表
     * @param pageDTO
     * @param userIdFuzzy
     * @param emailFuzzy
     * @return
     */
    @PostMapping("/loadBeautyAccountList")
    @GlobalInterceptor(checkAdmin = true)
    @PageQueryAutoFill
    public ResponseVO<PageResultVO> loadBeautyAccountList(@Valid PageDTO pageDTO, String userIdFuzzy, String emailFuzzy){
        PageResultVO pageResultVO = adminService.loadBeautyAccountList(pageDTO,userIdFuzzy,emailFuzzy);
        return ResponseUtils.success(pageResultVO);
    }

    /**
     * 新增或修改靓号
     * @param userInfoBeauty
     */
    @PostMapping("/saveBeautAccount")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveBeautyAccount(UserInfoBeauty userInfoBeauty){
        adminService.saveBeautyAccount(userInfoBeauty);
        return ResponseUtils.success();
    }

    /**
     * 删除靓号
     * @param id
     * @return
     */
    @PostMapping("/delBeautAccount")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO deleteBeautyAccount(@NotNull Integer id){
        adminService.deleteBeautyAccount(id);
        return ResponseUtils.success();
    }
}
