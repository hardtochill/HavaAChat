package cn.havaachat.mapper;

import cn.havaachat.annotation.AutoFill;
import cn.havaachat.enums.OperationTypeEnum;
import cn.havaachat.pojo.entity.UserInfoBeauty;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserInfoBeautyMapper {
    /**
     * 根据id查找靓号
     * @param id
     * @return
     */
    @Select("select * from user_info_beauty where id=#{id}")
    UserInfoBeauty findById(Long id);

    /**
     * 根据邮箱查找靓号
     * @param email
     * @return
     */
    @Select("select * from user_info_beauty where email=#{email}")
    UserInfoBeauty findByEmail(String email);
    /**
     * 根据userId查找靓号
     * @param userId
     * @return
     */
    @Select("select * from user_info_beauty where user_id=#{userId}")
    UserInfoBeauty findByUserId(String userId);

    /**
     * 修改
     * @param userInfoBeauty
     */
    @AutoFill(OperationTypeEnum.UPDATE)
    void update(UserInfoBeauty userInfoBeauty);

    /**
     * 插入
     * @param userInfoBeauty
     */
    @AutoFill(OperationTypeEnum.INSERT)
    void insert(UserInfoBeauty userInfoBeauty);

    /**
     * 分页查询
     * @param userInfoBeauty
     * @return
     */
    Page<UserInfoBeauty> findBatch(UserInfoBeauty userInfoBeauty);

    /**
     * 删除
     * @param id
     */
    @Delete("delete from user_info_beauty where id=#{id}")
    void deleteById(Integer id);
}
