package cn.havaachat.enums;

/**
 * 消息类型枚举
 */
public enum MessageTypeEnum {
    INIT(0,"","连接WS获取消息"),
    ADD_FRIEND(1,"","添加好友打招呼消息"),
    CHAT(2,"","普通聊天消息"),
    GROUP_CREATE(3,"群组已经创建好，可以和好友一起畅聊了","群创建成功"),
    CONTACT_APPLY(4,"","好友申请"),
    MEDIA_CHAT(5,"","媒体文件"),
    FILE_UPLOAD(6,"","文件上传完成"),
    FORCE_OFF_LINE(7,"","强制下线"),
    DISSOLUTION_GROUP(8,"群聊已解散","解散群聊"),
    ADD_GROUP(9,"%s加入了群组","加入群聊"),
    GROUP_NAME_UPDATE(10,"","更新群昵称"),
    LEAVE_GROUP(11,"%s退出了群聊","退出群聊"),
    REMOVE_GROUP(12,"%s被管理员移出了群聊","被管理员移出了群聊");
    private Integer type;
    private String initMessage;
    private String description;
    MessageTypeEnum(Integer type,String initMessage,String description){
        this.type=type;
        this.initMessage = initMessage;
        this.description = description;
    }
    public static MessageTypeEnum getByType(Integer type){
        for(MessageTypeEnum messageTypeEnum:MessageTypeEnum.values()){
            if (messageTypeEnum.getType().equals(type)){
                return messageTypeEnum;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }

    public String getInitMessage() {
        return initMessage;
    }

    public String getDescription() {
        return description;
    }
}
