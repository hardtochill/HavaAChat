package cn.havaachat.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 靓号是否已被使用
 */
public enum BeautyAccountStatusEnum {
    NO_USE(0,"未使用"),
    USED(1,"已使用");
    private Integer status;
    private String description;
    BeautyAccountStatusEnum(Integer status,String description){
        this.status = status;
        this.description = description;
    }
    public Integer getStatus(){
        return status;
    }
    public String getDescription(){
        return description;
    }

    /**
     * 根据状态获取枚举
     * @param status
     * @return
     */
    public BeautyAccountStatusEnum getByStatus(Integer status){
        for(BeautyAccountStatusEnum beautyAccountStatusEnum:BeautyAccountStatusEnum.values()){
            if(beautyAccountStatusEnum.getStatus().equals(status)){
                return beautyAccountStatusEnum;
            }
        }
        return null;
    }
}
