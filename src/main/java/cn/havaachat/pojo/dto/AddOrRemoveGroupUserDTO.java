package cn.havaachat.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 添加或移除群成员DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddOrRemoveGroupUserDTO {
    /**
     * 群聊id
     */
    @NotEmpty
    private String groupId;
    /**
     * 要添加或移除的群成员
     */
    @NotEmpty
    private String selectContacts;
    /**
     * 操作类型：添加or移除
     */
    @NotNull
    private Integer opType;
}
