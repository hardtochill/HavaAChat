package cn.havaachat.mapper;

import cn.havaachat.annotation.AutoFill;
import cn.havaachat.enums.OperationTypeEnum;
import cn.havaachat.pojo.entity.UserContact;
import cn.havaachat.pojo.entity.UserContactApply;
import cn.havaachat.pojo.vo.UserContactLoadResultVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserContactMapper {
    /**
     * 根据userId和contactId查询
     * @param userId
     * @param contactId
     * @return
     */
    @Select("select * from user_contact where user_id=#{userId} and contact_id=#{contactId}")
    UserContact findByUserIdAndContactId(String userId, String contactId);

    /**
     * 插入
     * @param userContact
     */
    @AutoFill(OperationTypeEnum.INSERT)
    void insert(UserContact userContact);

    /**
     * 根据联系人id和联系状态查询人数
     * @param contactId
     * @return
     */
    @Select("select count(*) from user_contact where contact_id=#{contactId} and status=#{status}")
    Integer countByContactIdAndStatus(String contactId,Integer status);

    /**
     * 根据群聊id，查询该群所有有效联系，并关联查询user_info表，查询每个联系人的名称和性别
     * @param contactId
     * @param status
     * @return
     */
    @Select("select uc.*, ui.nick_name as contactName,ui.sex " +
            "from user_contact uc inner join user_info ui on uc.user_id = ui.user_id " +
            "where uc.contact_id=#{contactId} and uc.status=#{status} " +
            "order by create_time asc")
    List<UserContact> findBatchWithContactNameAndSexByContactIdAndStatus(String contactId,Integer status);

    /**
     * 查询用户的联系人列表
     * @param userId 用户id
     * @param contactType 好友或群聊
     * @param statusList 联系状态
     * @return
     */
    List<UserContactLoadResultVO> findBatchWithContactNameByUserIdAndContactTypeAndStatusList(String userId,Integer contactType,@Param("statusList") List<Integer> statusList);

    /**
     * 查询用户的联系人列表
     * @param userId
     * @param status
     * @return
     */
    @Select("select * from user_contact where user_id=#{userId} and status=#{status};")
    List<UserContact> findBatchByUserIdAndStatus(String userId,Integer status);
    /**
     * 批量插入
     * @param userContactList
     */
    @AutoFill(OperationTypeEnum.INSERT)
    void insertBatch(@Param("userContactList") List<UserContact> userContactList);
    /**
     * 根据userId和contactId修改
     * @param userContact
     */
    @AutoFill(OperationTypeEnum.UPDATE)
    void updateByUserIdAndContactId(UserContact userContact);
    /**
     * 批量修改
     * @param userContactList
     */
    @AutoFill(OperationTypeEnum.UPDATE)
    void updateBatch(@Param("userContactList") List<UserContact> userContactList);

    /**
     * 根据contactId更新联系状态
     * @param status
     * @param contactId
     */
    @AutoFill(OperationTypeEnum.UPDATE)
    @Delete("update user_contact set status=#{status} where contact_id=#{contactId}")
    void updateStatusByContactId(Integer status,String contactId);
}
