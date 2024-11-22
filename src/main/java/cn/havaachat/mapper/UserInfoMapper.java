package cn.havaachat.mapper;

import cn.havaachat.annotation.AutoFill;
import cn.havaachat.enums.OperationTypeEnum;
import cn.havaachat.pojo.entity.UserInfo;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserInfoMapper {
    /**
     * 根据id查询
     * @param userId
     * @return
     */
    @Select("select * from user_info where user_id = #{userId}")
    UserInfo findById(String userId);

    /**
     * 分页查询
     * @return
     */
    Page<UserInfo> findBatch(UserInfo userInfo);
    /**
     * 根据邮箱查询
     * @param email
     * @return
     */
    @Select("select * from user_info where email=#{email}")
    public UserInfo findByEmail(String email);

    /**
     * 单个插入
     * @param userInfo
     */
    @AutoFill(OperationTypeEnum.INSERT)
    void insert(UserInfo userInfo);

    /**
     * 单个修改
     * @param userInfo
     */
    @AutoFill(OperationTypeEnum.UPDATE)
    void update(UserInfo userInfo);
}
