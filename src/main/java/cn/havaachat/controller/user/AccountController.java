package cn.havaachat.controller.user;

import cn.havaachat.annotation.GlobalInterceptor;
import cn.havaachat.pojo.dto.LoginDTO;
import cn.havaachat.pojo.dto.RegisterDTO;
import cn.havaachat.pojo.dto.SysSettingDTO;
import cn.havaachat.pojo.dto.TokenUserInfoDTO;
import cn.havaachat.pojo.vo.ResponseVO;
import cn.havaachat.pojo.vo.UserInfoVO;
import cn.havaachat.redis.RedisService;
import cn.havaachat.service.AccountService;
import cn.havaachat.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/account")
@Slf4j
public class AccountController {
    private AccountService accountService;
    private RedisService redisService;
    @Autowired
    public AccountController(AccountService accountService,RedisService redisService){
        this.accountService = accountService;
        this.redisService = redisService;
    }
    /**
     * 验证码
     * @return
     */
    @PostMapping("/checkCode")
    public ResponseVO checkCode(){
        return ResponseUtils.success(accountService.generateCheckCode());
    }

    /**
     * 注册
     * @param registerDTO
     * @return
     */
    @PostMapping("/register")
    public ResponseVO register(@Valid RegisterDTO registerDTO){
        accountService.checkCheckCodeAndRegister(registerDTO);
        return ResponseUtils.success();
    }

    /**
     * 登录
     * @param loginDTO
     * @return
     */
    @PostMapping("/login")
    public ResponseVO<UserInfoVO> login(@Valid LoginDTO loginDTO){
        UserInfoVO userInfoVO = accountService.checkCheckCodeAndLogin(loginDTO);
        return ResponseUtils.success(userInfoVO);
    }

    /**
     * 获取系统设置
     * @return
     */
    @GlobalInterceptor
    @PostMapping("/getSysSetting")
    public ResponseVO<SysSettingDTO> getSysSetting(){
        log.info("获取系统信息");
        return ResponseUtils.success(redisService.getSysSetting());
    }
}
