package cn.havaachat.enums;

/**
 * 版本发布状态
 */
public enum AppUpdateStatusEnum {
    INIT(0,"未发布"),
    GRAYSCALE(1,"灰度发布"),
    ALL(2,"全网发布");
    private Integer status;
    private String description;
    AppUpdateStatusEnum(Integer status,String description){
        this.status=status;
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }
    public static AppUpdateStatusEnum getByStatus(Integer status){
        for(AppUpdateStatusEnum appUpdateStatusEnum:AppUpdateStatusEnum.values()){
            if(appUpdateStatusEnum.getStatus().equals(status)){
                return appUpdateStatusEnum;
            }
        }
        return null;
    }
}
