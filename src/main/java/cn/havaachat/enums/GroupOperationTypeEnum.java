package cn.havaachat.enums;

/**
 * 群聊操作类型枚举
 */
public enum GroupOperationTypeEnum {
    REMOVE(0,"移除群员"),
    ADD(1,"添加群员");
    private Integer type;
    private String description;

    GroupOperationTypeEnum(Integer type,String description){
        this.type = type;
        this.description = description;
    }
    public GroupOperationTypeEnum getByType(Integer type){
        for (GroupOperationTypeEnum groupOperationTypeEnum : GroupOperationTypeEnum.values()) {
            if (groupOperationTypeEnum.getType().equals(type)){
                return groupOperationTypeEnum;
            }
        }
        return null;
    }
    public Integer getType(){
        return type;
    }
}
