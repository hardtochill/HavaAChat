package cn.havaachat.mapper;

import cn.havaachat.annotation.AutoFill;
import cn.havaachat.enums.OperationTypeEnum;
import cn.havaachat.pojo.entity.UserContactApply;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface UserContactApplyMapper {
    /**
     * 根据主键查询
     * @param applyId
     * @return
     */
    @Select("select * from user_contact_apply where apply_id=#{applyId}")
    UserContactApply findByApplyId(Integer applyId);

    /**
     * 根据联合索引查询
     * @param applyUserId
     * @param receiveUserId
     * @param contactId
     * @return
     */
    @Select("select * from user_contact_apply where apply_user_id=#{applyUserId} and receive_user_id=#{receiveUserId} and contact_id=#{contactId}")
    UserContactApply findByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId,String receiveUserId,String contactId);

    /**
     * 更新
     * @param userContactApply
     */
    @AutoFill(OperationTypeEnum.UPDATE)
    void update(UserContactApply userContactApply);

    /**
     * 删除
     * @param userContactApply
     */
    @AutoFill(OperationTypeEnum.INSERT)
    void insert(UserContactApply userContactApply);

    /**
     * 根据receiveUserId批量查询
     * @param receiveUserId
     * @return
     */
    Page<UserContactApply> findBatchWithContactNameByReceiveUserId(String receiveUserId);

    /**
     * 根据applyId和初始status修改status
     * @param applyId
     * @param originStatus
     * @param targetStatus
     * @return
     */
    @Update("update user_contact_apply set status=#{targetStatus},update_time=#{updateTime} where apply_id=#{applyId} and status=#{originStatus}")
    Integer updateStatusAndUpdateTimeByApplyIdAndStatus(Integer applyId, Integer originStatus, Integer targetStatus, LocalDateTime updateTime);

    /**
     * 根据接收者id和申请状态，查询申请数量
     * @param receiveUserId
     * @param status
     * @return
     */
    @Select("select count(*) from user_contact_apply where receive_user_id=#{receiveUserId} and status=#{status}")
    Integer countByReceiveUserIdAndStatus(String receiveUserId,Integer status);
}
