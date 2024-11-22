package cn.havaachat.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * 保存用户信息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveUserInfoDTO {
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
     * 头像文件
     */
    private MultipartFile avatarFile;
    /**
     * 头像文件缩略图
     */
    private MultipartFile avatarCover;
}
