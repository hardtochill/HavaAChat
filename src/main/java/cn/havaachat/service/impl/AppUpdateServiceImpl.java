package cn.havaachat.service.impl;

import cn.havaachat.config.AppConfiguration;
import cn.havaachat.enums.AppUpdateFileTypeEnum;
import cn.havaachat.enums.AppUpdateStatusEnum;
import cn.havaachat.enums.ResponseCodeEnum;
import cn.havaachat.exception.BaseException;
import cn.havaachat.mapper.AppUpdateMapper;
import cn.havaachat.pojo.dto.AppUpdateDTO;
import cn.havaachat.pojo.dto.PageDTO;
import cn.havaachat.pojo.entity.AppUpdate;
import cn.havaachat.pojo.vo.AppUpdateVO;
import cn.havaachat.pojo.vo.PageResultVO;
import cn.havaachat.service.AppUpdateService;
import cn.havaachat.utils.FilePathUtils;
import cn.havaachat.utils.StringUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 版本更新
 */
@Service
@Slf4j
public class AppUpdateServiceImpl implements AppUpdateService {
    private AppUpdateMapper appUpdateMapper;
    private AppConfiguration appConfiguration;
    public AppUpdateServiceImpl(AppUpdateMapper appUpdateMapper,AppConfiguration appConfiguration){
        this.appUpdateMapper=appUpdateMapper;
        this.appConfiguration=appConfiguration;
    }
    /**
     * 获取版本跟新列表
     * @param pageDTO
     * @param createDateStart
     * @param createDateEnd
     * @return
     */
    @Override
    public PageResultVO loadAppUpdateList(PageDTO pageDTO, LocalDate createDateStart, LocalDate createDateEnd) {
        PageHelper.startPage(pageDTO.getPageNo(), pageDTO.getPageSize());
        LocalDateTime createDateTimeStart = null;
        LocalDateTime createDateTimeEnd = null;
        // LocalDate转LocalDateTime
        if(null!=createDateStart && null!=createDateEnd){
            createDateTimeStart = createDateStart.atStartOfDay();
            createDateTimeEnd = createDateEnd.atTime(23, 59, 59);
        }
        log.info("后台管理：获取版本更新列表：pageDTO：{}，createTimeStart：{}，createTimeEnd：{}",pageDTO,createDateTimeStart,createDateTimeEnd);
        Page<AppUpdate> page = appUpdateMapper.findBatchByCreateTimeZone(createDateTimeStart, createDateTimeEnd);
        return new PageResultVO(pageDTO.getPageNo(), pageDTO.getPageSize(), page.getTotal(), page.getResult());
    }
    /**
     * 新增或修改版本
     * @param appUpdateDTO
     */
    @Override
    public void saveAppUpdate(AppUpdateDTO appUpdateDTO) {
        log.info("后台管理：新增或修改版本：appUpdateVersion：{}",appUpdateDTO.getVersion());
        AppUpdateFileTypeEnum appUpdateFileType = AppUpdateFileTypeEnum.getByType(appUpdateDTO.getFileType());
        if(null==appUpdateFileType){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }

        // 已发布的版本不能修改
        if(null!=appUpdateDTO.getId()){
            AppUpdate existAppUpdate = appUpdateMapper.findById(appUpdateDTO.getId());
            if(null!=existAppUpdate && !AppUpdateStatusEnum.INIT.getStatus().equals(existAppUpdate.getStatus())){
                throw new BaseException(ResponseCodeEnum.CODE_600);
            }
        }

        List<AppUpdate> historyAppUpdateList = appUpdateMapper.findBatchOrderByIdDesc();
        if(!historyAppUpdateList.isEmpty()){
            // 历史最大版本
            AppUpdate histroyBiggestAppUpdate = historyAppUpdateList.get(0);
            // replaceAll()用的是正则表达式，正则表达式中两个“\”才能表示一个“\”，所以 正则表达式的”\\.“=普通表示的“\.”=特殊字符“.”
            long historyBiggestAppUpdateVersion = Long.parseLong(histroyBiggestAppUpdate.getVersion().replaceAll("\\.", ""));
            // 当前要修改的版本
            long currentAppUpdateVersion = Long.parseLong(appUpdateDTO.getVersion().replaceAll("\\.", ""));

            // 如果是新增版本，新增版本号必须大于历史最大版本号
            if(null==appUpdateDTO.getId() && currentAppUpdateVersion<=historyBiggestAppUpdateVersion){
                throw new BaseException(ResponseCodeEnum.CODE_400.getCode(),"当前版本必须大于历史最大版本");
            }
            // 如果是修改版本，修改内容如果包括版本号，则修改的版本号不能大于历史最大版本号；除非自己已经是历史最大版本号，要改得更大
            if(null!=appUpdateDTO.getId() && currentAppUpdateVersion>=historyBiggestAppUpdateVersion && !appUpdateDTO.getId().equals(histroyBiggestAppUpdate.getId())){
                throw new BaseException(ResponseCodeEnum.CODE_400.getCode(),"当前版本必须小于历史最大版本");
            }
            // 要新增或修改的版本号不能存在
            AppUpdate existAppUpdate = appUpdateMapper.findByVersion(appUpdateDTO.getVersion());
            if(null!=existAppUpdate){
                throw new BaseException(ResponseCodeEnum.CODE_400.getCode(),"当前版本已存在");
            }
        }
        AppUpdate appUpdate = new AppUpdate();
        BeanUtils.copyProperties(appUpdateDTO,appUpdate);
        // 新增版本
        if(null==appUpdateDTO.getId()){
            appUpdate.setStatus(AppUpdateStatusEnum.INIT.getStatus());
            appUpdateMapper.insert(appUpdate);
        }else{ // 修改版本
            appUpdateMapper.update(appUpdate);
        }
        // 将版本文件存至本地
        if(null!=appUpdateDTO.getFile()){
            String appUpdateFileFolderPath = FilePathUtils.generateAppUpdateFileFolderPath(appConfiguration.getFileFolder());
            File appUpdateFileFolder = new File(appUpdateFileFolderPath);
            if(!appUpdateFileFolder.exists()){
                appUpdateFileFolder.mkdirs();
            }
            try{
                appUpdateDTO.getFile().transferTo(new File(FilePathUtils.generateAppUpdateFilePath(appUpdateFileFolderPath, appUpdate.getId())));
            }catch (IOException e){
                throw new BaseException(ResponseCodeEnum.CODE_500);
            }
        }
    }

    /**
     * 删除版本
     * @param id
     */
    @Override
    public void deleteAppUpdate(Integer id) {
        log.info("后台管理：删除版本：id：{}",id);
        // 已发布的版本不能删除
        AppUpdate existAppUpdate = appUpdateMapper.findById(id);
        if(null!=existAppUpdate && !AppUpdateStatusEnum.INIT.getStatus().equals(existAppUpdate.getStatus())){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        appUpdateMapper.deleteById(id);
    }

    /**
     * 发布版本
     * @param id
     * @param status
     * @param grayscaleUid
     */
    @Override
    public void postAppUpdate(Integer id, Integer status, String grayscaleUid) {
        log.info("后台管理：发布版本：id：{}，status：{}，grayscaleUid：{}",id,status,grayscaleUid);
        AppUpdateStatusEnum appUpdateStatusEnum = AppUpdateStatusEnum.getByStatus(status);
        if(null==appUpdateStatusEnum){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        // 灰度发布则发布列表不能为空
        if(AppUpdateStatusEnum.GRAYSCALE.equals(appUpdateStatusEnum) && StringUtils.isEmpty(grayscaleUid)){
            throw new BaseException(ResponseCodeEnum.CODE_600);
        }
        if(!AppUpdateStatusEnum.GRAYSCALE.equals(appUpdateStatusEnum)){
            grayscaleUid = null;
        }
        AppUpdate appUpdate = new AppUpdate();
        appUpdate.setId(id);
        appUpdate.setStatus(status);
        appUpdate.setGrayscaleUid(grayscaleUid);
        appUpdateMapper.update(appUpdate);
    }

    /**
     * 获取最新版本
     * @param appVersion 用户当前版本
     * @param uid 用户id
     * @return
     */
    @Override
    public AppUpdateVO getLatestAppUpdate(String appVersion, String uid) {
        log.info("获取最新版本：appVersion：{}，uid：{}",appVersion,uid);
        // 前端每次打开都会自动调该接口，因此会出现未传appVersion的情况，此处不抛异常
        if(StringUtils.isEmpty(appVersion)){
            return null;
        }
        AppUpdate latestAppUpdate = appUpdateMapper.findLatestAppUpdateByVersionAndUid(appVersion,uid);
        // 未检测到新版本
        if(null==latestAppUpdate){
            return null;
        }
        AppUpdateVO latestAppUpdateVO = new AppUpdateVO();
        BeanUtils.copyProperties(latestAppUpdate,latestAppUpdateVO);
        // 更新描述
        latestAppUpdateVO.setUpdateList(Arrays.asList(latestAppUpdate.getUpdateDescArray()));
        // 文件大小
        if(AppUpdateFileTypeEnum.LOCAL.getType().equals(latestAppUpdate.getFileType())){
            String appUpdateFileFolderPath = FilePathUtils.generateAppUpdateFileFolderPath(appConfiguration.getFileFolder());
            String appUpdateFilePath = FilePathUtils.generateAppUpdateFilePath(appUpdateFileFolderPath, latestAppUpdate.getId());
            latestAppUpdateVO.setSize(new File(appUpdateFilePath).length());
        }else{
            latestAppUpdateVO.setSize(0L);
        }
        // 文件名
        String latestAppUpdateFileName = StringUtils.getAppUpdateFileName(latestAppUpdate.getVersion());
        latestAppUpdateVO.setFileName(latestAppUpdateFileName);
        return latestAppUpdateVO;
    }
}
