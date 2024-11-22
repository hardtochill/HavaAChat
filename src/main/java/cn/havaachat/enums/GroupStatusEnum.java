package cn.havaachat.enums;

/**
 * 群聊状态枚举
 */
public enum GroupStatusEnum {
    DISSOLUTION(0,"已解散"),
    NORMAL(1,"正常");
    private Integer status;
    private String description;
    GroupStatusEnum(Integer status,String description){
        this.status = status;
        this.description = description;
    }
    public Integer getStatus(){
        return status;
    }
    public String getDescription(){
        return description;
    }
    public GroupStatusEnum getByStatus(Integer status){
        for(GroupStatusEnum groupStatusEnum:GroupStatusEnum.values()){
            if(groupStatusEnum.getStatus().equals(status)){
                return groupStatusEnum;
            }
        }
        return null;
    }
}
