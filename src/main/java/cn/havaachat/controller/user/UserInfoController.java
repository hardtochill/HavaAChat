package cn.havaachat.controller.user;

import cn.havaachat.annotation.GlobalInterceptor;
import cn.havaachat.constants.AccountConstants;
import cn.havaachat.pojo.dto.SaveUserInfoDTO;
import cn.havaachat.pojo.vo.ResponseVO;
import cn.havaachat.pojo.vo.UserInfoVO;
import cn.havaachat.service.UserInfoService;
import cn.havaachat.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.IOException;

@RestController
@RequestMapping("/userInfo")
@Validated
public class UserInfoController {
    private UserInfoService userInfoService;
    @Autowired
    public UserInfoController(UserInfoService userInfoService){
        this.userInfoService = userInfoService;
    }

    /**
     * 获取用户信息
     * @return
     */
    @PostMapping("/getUserInfo")
    @GlobalInterceptor
    public ResponseVO<UserInfoVO> getUserInfo(){
        UserInfoVO userInfoVO = userInfoService.getUserInfo();
        return ResponseUtils.success(userInfoVO);
    }

    /**
     * 保存用户信息
     * @param saveUserInfoDTO
     * @return
     */
    @PostMapping("/saveUserInfo")
    @GlobalInterceptor
    public ResponseVO<UserInfoVO> saveUserInfo(@Valid SaveUserInfoDTO saveUserInfoDTO) throws IOException {
        userInfoService.saveUserInfo(saveUserInfoDTO);
        UserInfoVO userInfoVO = userInfoService.getUserInfo();
        return ResponseUtils.success(userInfoVO);
    }

    /**
     * 修改密码
     * @param password
     * @return
     */
    @PostMapping("/updatePassword")
    @GlobalInterceptor
    public ResponseVO updatePassword(@NotEmpty @Pattern(regexp = AccountConstants.REGEX_PASSWORD)String password){
        userInfoService.updatePassword(password);
        return ResponseUtils.success();
    }

    /**
     * 退出登录
     * @return
     */
    @PostMapping("/logout")
    @GlobalInterceptor
    public ResponseVO logout(){
        userInfoService.logout();
        return ResponseUtils.success();
    }
}
