package cn.havaachat.mapper;

import cn.havaachat.annotation.AutoFill;
import cn.havaachat.enums.OperationTypeEnum;
import cn.havaachat.pojo.entity.AppUpdate;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 版本更新mapper
 */
@Mapper
public interface AppUpdateMapper {
    /***
     * 根据id查找
     * @param id
     * @return
     */
    @Select("select * from app_update where id=#{id}")
    AppUpdate findById(Integer id);

    Page<AppUpdate> findBatchByCreateTimeZone(LocalDateTime createTimeStart,LocalDateTime createTimeEnd);

    /**
     * 查询全部，并根据id倒序
     * @return
     */
    @Select("select * from app_update order by id desc")
    List<AppUpdate> findBatchOrderByIdDesc();

    /**
     * 根据版本号查询
     * @param version
     * @return
     */
    @Select("select * from app_update where version=#{version}")
    AppUpdate findByVersion(String version);

    /**
     * 根据id删除
     * @param id
     */
    @Delete("delete from app_update where id=#{id}")
    void deleteById(Integer id);

    @AutoFill(OperationTypeEnum.UPDATE)
    void update(AppUpdate appUpdate);

    /**
     * 插入
     * @param appUpdate
     */
    @AutoFill(OperationTypeEnum.INSERT)
    void insert(AppUpdate appUpdate);

    /**
     * 查找最新版本
     * @param version
     * @param uid
     * @return
     */
    AppUpdate findLatestAppUpdateByVersionAndUid(String version,String uid);
}
