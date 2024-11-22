package cn.havaachat.enums;

/**
 * 用户与联系人的状态
 */
public enum UserContactStatusEnum {
    NOT_FRIEND(0,"非好友"),
    FRIEND(1,"好友"),
    DEL(2,"已删除好友"),
    DEL_BE(3,"被好友删除"),
    BLACKLIST(4,"已拉黑好友"),
    BLACKLIST_BE(5,"被好友拉黑"),
    BLACKLIST_BE_FIRST(6,"还未添加好友就被拉黑");
    private Integer status;
    private String description;
    UserContactStatusEnum(Integer status,String description){
        this.status = status;
        this.description  = description;
    }
    public Integer getStatus(){
        return status;
    }
    public String getDescription(){
        return description;
    }
    public static UserContactStatusEnum getByStatus(Integer status){
        for(UserContactStatusEnum userContactStatusEnum:UserContactStatusEnum.values()){
            if(userContactStatusEnum.getStatus().equals(status)){
                return userContactStatusEnum;
            }
        }
        return null;
    }
}
