package cn.havaachat.controller.admin;

import cn.havaachat.annotation.GlobalInterceptor;
import cn.havaachat.pojo.dto.SysSettingDTO;
import cn.havaachat.pojo.vo.ResponseVO;
import cn.havaachat.redis.RedisService;
import cn.havaachat.service.AdminService;
import cn.havaachat.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 后台管理：群聊
 */
@RestController
@RequestMapping("/admin")
@Validated
public class AdminSysSettingController {
    private AdminService adminService;
    private RedisService redisService;
    @Autowired
    public AdminSysSettingController(AdminService adminService,RedisService redisService){
        this.adminService = adminService;
        this.redisService = redisService;
    }

    @PostMapping("/getSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO<SysSettingDTO> getSysSetting(){
        return ResponseUtils.success(redisService.getSysSetting());
    }
    @PostMapping("/saveSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveSysSetting(SysSettingDTO sysSettingDTO, MultipartFile robotFile,MultipartFile robotCover){
        adminService.saveSysSetting(sysSettingDTO,robotFile,robotCover);
        return ResponseUtils.success();
    }
}
