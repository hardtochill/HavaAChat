package cn.havaachat.controller.user;

import cn.havaachat.annotation.GlobalInterceptor;
import cn.havaachat.pojo.vo.AppUpdateVO;
import cn.havaachat.pojo.vo.ResponseVO;
import cn.havaachat.service.AppUpdateService;
import cn.havaachat.utils.ResponseUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户版本更新
 */
@RestController
@RequestMapping("/update")
public class UserAppUpdateController {
    private AppUpdateService appUpdateService;
    public UserAppUpdateController(AppUpdateService appUpdateService){
        this.appUpdateService=appUpdateService;
    }

    /**
     * 检测更新
     * @param appVersion
     * @param uid
     * @return
     */
    @PostMapping("/checkVersion")
    @GlobalInterceptor
    public ResponseVO<AppUpdateVO> checkVersion(String appVersion,String uid){
        AppUpdateVO appUpdateVO = appUpdateService.getLatestAppUpdate(appVersion,uid);
        return ResponseUtils.success(appUpdateVO);
    }
}
