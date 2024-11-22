package cn.havaachat.enums;

/**
 * 消息发送状态枚举
 */
public enum MessageStatusEnum {
    SENDING(0,"发送中"),
    SENDED(1,"已发送");
    private Integer status;
    private String description;
    MessageStatusEnum(Integer status,String description){
        this.status=status;
        this.description = description;
    }

    public static MessageStatusEnum getByStatus(Integer status){
        for(MessageStatusEnum messageStatusEnum:MessageStatusEnum.values()){
            if (messageStatusEnum.getStatus().equals(status)){
                return messageStatusEnum;
            }
        }
        return null;
    }
    public Integer getStatus() {
        return status;
    }
    public String getDescription() {
        return description;
    }
}
