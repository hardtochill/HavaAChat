package cn.havaachat.pojo.dto;

import cn.havaachat.constants.AccountConstants;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统设置
 */
@Data
public class SysSettingDTO implements Serializable {
    /**
     * 最大群组数
     */
    private Integer maxGroupCount=5;
    /**
     * 群组最大人数
     */
    private Integer maxGroupMemberCount=500;
    /**
     * 允许上传最大图片 MB
     */
    private Integer maxImageSize = 5;
    /**
     * 允许上传最大视频 MB
     */
    private Integer maxVideoSize = 10;
    /**
     * 允许上传最大文件 MB
     */
    private Integer maxFileSize = 15;
    /**
     * 聊天机器人uid
     */
    private String robotUid = AccountConstants.ROBOT_UID;
    /**
     * 聊天机器人名字
     */
    private String robotNickName = "HavaAChat";
    /**
     * 聊天机器人初始发送语句
     */
    private String robotWelcome = "欢迎使用HavaAChat";
    /**
     * 聊天机器人自动回复语句
     */
    private String robotAutoResponse = "我只是一个机器人";
}
