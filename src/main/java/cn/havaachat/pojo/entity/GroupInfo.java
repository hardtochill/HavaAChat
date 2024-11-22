package cn.havaachat.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 群组信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupInfo extends BaseEntity{
    /**
     * 主键id
     */
    private String groupId;
    /**
     * 群组名称
     */
    private String groupName;
    /**
     * 群主id
     */
    private String groupOwnerId;
    /**
     * 群主名称
     */
    private String groupOwnerNickName;
    /**
     * 群公告
     */
    private String groupNotice;
    /**
     * 加入方式 0：自由加入，1：需要验证
     */
    private Integer joinType;
    /**
     * 群状态 0：解散，1：正常
     */
    private Integer status;
    /**
     * 群成员数量
     */
    private Integer memberCount;
}
