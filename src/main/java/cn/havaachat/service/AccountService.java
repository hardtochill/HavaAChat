package cn.havaachat.service;

import cn.havaachat.pojo.dto.LoginDTO;
import cn.havaachat.pojo.dto.RegisterDTO;
import cn.havaachat.pojo.dto.TokenUserInfoDTO;
import cn.havaachat.pojo.vo.UserInfoVO;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Target;
import java.util.Map;

public interface AccountService {
    /**
     * 生成验证码
     * @return
     */
    Map<String,String> generateCheckCode();
    /**
     * 校验验证码并注册
     * @param registerDTO
     */
    void checkCheckCodeAndRegister(RegisterDTO registerDTO);
    /**
     * 校验验证码并登录
     * @param loginDTO
     */
    UserInfoVO checkCheckCodeAndLogin(LoginDTO loginDTO);
}
