package cn.havaachat.service;

import cn.havaachat.pojo.dto.AppUpdateDTO;
import cn.havaachat.pojo.dto.PageDTO;
import cn.havaachat.pojo.vo.AppUpdateVO;
import cn.havaachat.pojo.vo.PageResultVO;

import java.time.LocalDate;

/**
 * 版本更新
 */
public interface AppUpdateService {
    /**
     * 获取版本列表
     * @param pageDTO
     * @param createDateStart
     * @param createDateEnd
     * @return
     */
    PageResultVO loadAppUpdateList(PageDTO pageDTO, LocalDate createDateStart, LocalDate createDateEnd);

    /**
     * 新增或修改版本
     * @param appUpdateDTO
     */
    void saveAppUpdate(AppUpdateDTO appUpdateDTO);

    /**
     * 删除版本
     * @param id
     */
    void deleteAppUpdate(Integer id);

    /**
     * 发布版本
     * @param id
     * @param status
     * @param grayscaleUid
     */
    void postAppUpdate(Integer id, Integer status, String grayscaleUid);

    /**
     * 获取最新版本
     * @param appVersion 用户当前版本
     * @param uid 用户id
     * @return
     */
    AppUpdateVO getLatestAppUpdate(String appVersion,String uid);
}
