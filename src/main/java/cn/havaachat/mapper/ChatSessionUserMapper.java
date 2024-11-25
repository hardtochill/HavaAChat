package cn.havaachat.mapper;

import cn.havaachat.annotation.AutoFill;
import cn.havaachat.enums.OperationTypeEnum;
import cn.havaachat.pojo.entity.ChatSessionUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatSessionUserMapper {
    /**
     * 根据用户id和联系人id查询
     */
    @Select("select * from chat_session_user where user_id=#{userId} and contact_id=#{contactId}")
    ChatSessionUser findByUserIdAndContactId(String userId,String contactId);
    /**
     * 批量查询用户会话对象及其lastMessage、lastReceiveTime和memberCount
     */
    List<ChatSessionUser> findBatchWithSessionByUserId(String userId);

    /**
     * 插入
     * @param chatSessionUser
     */
    @AutoFill(OperationTypeEnum.INSERT)
    @Insert("insert into chat_session_user values (#{userId},#{contactId},#{sessionId},#{contactName},#{createTime},#{updateTime})")
    void insert(ChatSessionUser chatSessionUser);

    /**
     * 批量插入
     * @param chatSessionUserList
     */
    @AutoFill(OperationTypeEnum.INSERT)
    void insertBatch(@Param("chatSessionUserList") List<ChatSessionUser> chatSessionUserList);

    /**
     * 批量修改
     * @param chatSessionUserList
     */
    @AutoFill(OperationTypeEnum.UPDATE)
    void updateBatch(@Param("chatSessionUserList") List<ChatSessionUser> chatSessionUserList);
}
