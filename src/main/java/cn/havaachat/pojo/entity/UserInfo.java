package cn.havaachat.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo extends BaseEntity{
    /**
     * 用户id 12位，1位标识符+11位数字
     */
    private String userId;
    /**
     * 用户邮箱
     */
    private String email;
    /**
     * 用户昵称
     */
    private String nickName;
    /**
     * 用户密码
     */
    private String password;
    /**
     * 加好友类型 0：直接加；1：同意后加
     */
    private Integer joinType;
    /**
     * 性别 0：女；1：男
     */
    private Integer sex;
    /**
     * 个性签名
     */
    private String personalSignature;
    /**
     * 所在区域名
     */
    private String areaName;
    /**
     * 所在区域编码
     */
    private String areaCode;
    /**
     * 状态 0：禁用，1：启用
     */
    private Integer status;
    /**
     * 最后登陆时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime lastLoginTime;
    /**
     * 最后离线时间
     */
    private Long lastOffTime;
}
