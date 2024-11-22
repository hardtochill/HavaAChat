package cn.havaachat.enums;

import cn.havaachat.utils.StringUtils;

/**
 * 用户or群组
 */
public enum UserContactTypeEnum {
    USER(0,"U","好友"),
    GROUP(1,"G","群组");
    private Integer type;
    private String prefix;
    private String description;
    UserContactTypeEnum(Integer type, String prefix, String description){
        this.type = type;
        this.prefix = prefix;
        this.description = description;
    }
    public Integer getType(){
        return type;
    }
    public String getPrefix(){
        return prefix;
    }
    public String getDescription(){
        return description;
    }

    /**
     * 根据名称获取枚举对象
     * @param name
     * @return
     */
    public static UserContactTypeEnum getByName(String name){
        try{
            if(StringUtils.isEmpty(name)){
                return null;
            }
            return UserContactTypeEnum.valueOf(name.toUpperCase());
        }catch (Exception e){
            return null;
        }
    }
    /**
     * 根据id前缀获取枚举对象
     * @param id
     * @return
     */
    public static UserContactTypeEnum getById(String id){
        if(StringUtils.isEmpty(id)){
            return null;
        }
        id = id.substring(0,1).toUpperCase();
        for(UserContactTypeEnum userContactTypeEnum : UserContactTypeEnum.values()){
            if (userContactTypeEnum.getPrefix().equals(id)){
                return userContactTypeEnum;
            }
        }
        return null;
    }
}
