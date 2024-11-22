package cn.havaachat.mapper;

import cn.havaachat.annotation.AutoFill;
import cn.havaachat.enums.OperationTypeEnum;
import cn.havaachat.pojo.entity.ChatMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
}
