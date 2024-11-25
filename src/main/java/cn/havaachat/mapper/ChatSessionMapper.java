package cn.havaachat.mapper;

import cn.havaachat.annotation.AutoFill;
import cn.havaachat.enums.OperationTypeEnum;
import cn.havaachat.pojo.entity.ChatSession;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ChatSessionMapper {
    /**
     * 根据会话id查询
     * @param sessionId
     * @return
     */
    @Select("select * from chat_session where session_id=#{sessionId}")
    ChatSession findBySessionId(String sessionId);

    /**
     * 插入
     * @param chatSession
     */
    @AutoFill(OperationTypeEnum.INSERT)
    @Insert("insert into chat_session values (#{sessionId},#{lastMessage},#{lastReceiveTime},#{createTime},#{updateTime})")
    void insert(ChatSession chatSession);

    /**
     * 更改
     * @param chatSession
     */
    @AutoFill(OperationTypeEnum.UPDATE)
    void update(ChatSession chatSession);
}
