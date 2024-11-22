package cn.havaachat.service;

import cn.havaachat.pojo.dto.SaveUserInfoDTO;
import cn.havaachat.pojo.vo.UserInfoVO;

import java.io.IOException;

public interface UserInfoService {
    /**
     * 获取用户信息
     * @return
     */
    UserInfoVO getUserInfo();

    /**
     * 保存用户信息
     * @param saveUserInfoDTO
     */
    void saveUserInfo(SaveUserInfoDTO saveUserInfoDTO) throws IOException;

    /**
     * 修改密码
     * @param password
     */
    void updatePassword(String password);
}
