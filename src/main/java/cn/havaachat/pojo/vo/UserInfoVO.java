package cn.havaachat.pojo.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoVO implements Serializable {
    /**
     * 用户id 12位，1位标识符+11位数字
     */
    private String userId;
    /**
     * 用户昵称
     */
    private String nickName;
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
     * 状态：0.非好友，1.好友，2.已删除好友，3.被好友删除，4.已拉黑好友，5.被好友拉黑，6.还未添加好友就被拉黑
     */
    private Integer contactStatus;
    /**
     * token
     */
    private String token;
    /**
     * 是否为超级管理员
     */
    private Boolean admin;
}
