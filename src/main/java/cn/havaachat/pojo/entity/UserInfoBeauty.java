package cn.havaachat.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 靓号实体类
 */
@Data
public class UserInfoBeauty extends BaseEntity{
    /**
     * id
     */
    private Long id;
    /**
     * 邮箱
     * 一个靓号绑定一个邮箱，如果用户使用该邮箱注册，则给其该靓号
     */
    private String email;
    /**
     * 用户id，即靓号，未加前缀，为11位数字
     */
    private String userId;
    /**
     * status 是否已被使用 0：否，1：是
     */
    private Integer status;
}
