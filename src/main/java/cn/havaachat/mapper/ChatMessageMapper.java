package cn.havaachat.mapper;

import cn.havaachat.annotation.AutoFill;
import cn.havaachat.annotation.PageQueryAutoFill;
import cn.havaachat.enums.OperationTypeEnum;
import cn.havaachat.pojo.entity.ChatMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    /**
     * 根据消息id查询
     * @param messageId
     * @return
     */
    @Select("select * from chat_message where message_id=#{messageId}")
    ChatMessage findById(Long messageId);

    /**
     * 插入
     * @param chatMessage
     */
    @AutoFill(OperationTypeEnum.INSERT)
    void insert(ChatMessage chatMessage);

    /**
     * 查询用户在指定时间内应该收到的所有消息
     * @param contactIdList
     * @param lastReceiveTime
     * @return
     */
    List<ChatMessage> findBatchByContactIdListAndTime(@Param("contactIdList") List<String> contactIdList, Long lastReceiveTime);

    /**
     * 更新
     * @param chatMessageForUpdate
     */
    @AutoFill(OperationTypeEnum.UPDATE)
    void update(ChatMessage chatMessageForUpdate);
}
