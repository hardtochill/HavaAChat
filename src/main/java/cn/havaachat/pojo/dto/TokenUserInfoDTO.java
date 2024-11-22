package cn.havaachat.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息Token类
 */
@Data
public class TokenUserInfoDTO implements Serializable {
    private String token;
    private String nickName;
    private String userId;
    private Boolean admin;
}
