package cn.havaachat.service;

import cn.havaachat.pojo.dto.AppUpdateDTO;
import cn.havaachat.pojo.dto.PageDTO;
import cn.havaachat.pojo.dto.SysSettingDTO;
import cn.havaachat.pojo.entity.UserInfoBeauty;
import cn.havaachat.pojo.vo.PageResultVO;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 后台管理
 */
public interface AdminService {
    /**
     * 获取用户列表
     * @param pageDTO
     * @param userId
     * @param nickNameFuzzy
     * @return
     */
    PageResultVO loadUser(PageDTO pageDTO,String userId,String nickNameFuzzy);

    /**
     * 更改用户状态：启用or禁用
     * @param status
     * @param userId
     */
    void updateUserStatus(Integer status,String userId);

    /**
     * 强制下线
     * @param userId
     */
    void forceOffLine(String userId);

    /**
     * 获取靓号列表
     * @param pageDTO
     * @param userIdFuzzy
     * @param emailFuzzy
     * @return
     */
    PageResultVO loadBeautyAccountList(PageDTO pageDTO,String userIdFuzzy,String emailFuzzy);

    /**
     * 新增或修改靓号
     * @param userInfoBeauty
     */
    void saveBeautyAccount(UserInfoBeauty userInfoBeauty);

    /**
     * 删除靓号
     * @param id
     */
    void deleteBeautyAccount(Integer id);


    /**
     * 保存系统设置
     * @param sysSettingDTO
     * @param robotFile
     * @param robotCover
     */
    void saveSysSetting(SysSettingDTO sysSettingDTO, MultipartFile robotFile, MultipartFile robotCover);


}
