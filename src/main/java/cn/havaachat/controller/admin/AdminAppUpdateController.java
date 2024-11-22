package cn.havaachat.controller.admin;

import cn.havaachat.annotation.GlobalInterceptor;
import cn.havaachat.annotation.PageQueryAutoFill;
import cn.havaachat.pojo.dto.AppUpdateDTO;
import cn.havaachat.pojo.dto.PageDTO;
import cn.havaachat.pojo.vo.PageResultVO;
import cn.havaachat.pojo.vo.ResponseVO;
import cn.havaachat.service.AdminService;
import cn.havaachat.service.AppUpdateService;
import cn.havaachat.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 后台管理：版本更新
 */
@RestController
@RequestMapping("/admin")
@Validated
public class AdminAppUpdateController {
    private AppUpdateService appUpdateService;
    @Autowired
    public AdminAppUpdateController(AppUpdateService appUpdateService){
        this.appUpdateService=appUpdateService;
    }

    /**
     * 获取版本列表
     * @param pageDTO
     * @param createTimeStart
     * @param createTimeEnd
     * @return
     */
    @PostMapping("/loadUpdateList")
    @GlobalInterceptor(checkAdmin = true)
    @PageQueryAutoFill
    public ResponseVO<PageResultVO> loadAppUpdateList(@Valid PageDTO pageDTO, LocalDate createTimeStart, LocalDate createTimeEnd){
        PageResultVO pageResultVO = appUpdateService.loadAppUpdateList(pageDTO, createTimeStart, createTimeEnd);
        return ResponseUtils.success(pageResultVO);
    }

    /**
     * 新增或修改版本
     * @param appUpdateDTO
     * @return
     */
    @PostMapping("/saveUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveAppUpdate(@Valid AppUpdateDTO appUpdateDTO){
        appUpdateService.saveAppUpdate(appUpdateDTO);
        return ResponseUtils.success();
    }

    /**
     * 删除版本
     * @param id
     * @return
     */
    @PostMapping("/delUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO deleteAppUpdate(@NotNull Integer id){
        appUpdateService.deleteAppUpdate(id);
        return ResponseUtils.success();
    }

    /**
     * 发布版本
     * @return
     */
    @PostMapping("/postUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO postAppUpdate(@NotNull Integer id,@NotNull Integer status,String grayscaleUid){
        appUpdateService.postAppUpdate(id,status,grayscaleUid);
        return ResponseUtils.success();
    }

}
