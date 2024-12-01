package cn.havaachat.enums;

/**
 * 退出群聊枚举
 */
public enum GroupLeaveTypeEnum {
    LEAVE(0,"主动离开"),
    REMOVE(1,"被移除");
    private Integer type;
    private String description;
    GroupLeaveTypeEnum(Integer type,String description){
        this.type = type;
        this.description = description;
    }

    public GroupLeaveTypeEnum getByType(Integer type){
        for (GroupLeaveTypeEnum groupLeaveTypeEnum : GroupLeaveTypeEnum.values()) {
            if (groupLeaveTypeEnum.getType().equals(type)){
                return groupLeaveTypeEnum;
            }
        }
        return null;
    }
    public Integer getType(){
        return type;
    }
}
