package cn.havaachat.enums;

/**
 * 申请添加联系人状态枚举
 */
public enum UserContactApplyStatusEnum {
    INIT(0,"待处理"),
    PASS(1,"已同意"),
    REJECT(2,"已拒绝"),
    BLACKLIST(3,"已拉黑");
    private Integer status;
    private String description;
    UserContactApplyStatusEnum(Integer status,String description){
        this.status = status;
        this.description  = description;
    }
    public Integer getStatus(){
        return status;
    }
    public String getDescription(){
        return description;
    }
    public static UserContactApplyStatusEnum getByStatus(Integer status){
        for(UserContactApplyStatusEnum userContactApplyStatusEnum:UserContactApplyStatusEnum.values()){
            if(userContactApplyStatusEnum.getStatus().equals(status)){
                return userContactApplyStatusEnum;
            }
        }
        return null;
    }
}
