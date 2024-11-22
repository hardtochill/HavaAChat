package cn.havaachat.enums;

import cn.havaachat.utils.StringUtils;

/**
 * 用户被添加好友类型
 */
public enum JoinTypeEnum {
    JOIN(0,"直接加"),
    APPLY(1,"需要审核");
    private Integer type;
    private String description;
    JoinTypeEnum(Integer type,String description){
        this.type = type;
        this.description = description;
    }

    public Integer getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
    public static JoinTypeEnum getByName(String name){
        try{
            if(StringUtils.isEmpty(name)){
                return null;
            }
            return JoinTypeEnum.valueOf(name.toUpperCase());
        }catch (Exception e){
            return null;
        }
    }
    public static JoinTypeEnum getByType(Integer type){
        for(JoinTypeEnum joinTypeEnum : JoinTypeEnum.values()){
            if (joinTypeEnum.getType().equals(type)){
                return joinTypeEnum;
            }
        }
        return null;
    }
}
