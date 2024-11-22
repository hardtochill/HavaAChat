package cn.havaachat.constants;

import cn.havaachat.enums.UserContactTypeEnum;

/**
 * 登陆注册部分常量
 */
public class AccountConstants {
    public static final String CHECKCODE_WRONG = "验证码错误";
    public static final String REGISTER_USER_EXISTED = "用户已存在";
    public static final String LOGIN_WRONG = "账号或密码错误";
    public static final String LOGIN_USER_DISABLE = "用户已禁用";
    public static final String LOGIN_USER_LOGIN_ALREADY = "用户已在别处登录";
    public static final String ROBOT_UID = UserContactTypeEnum.USER.getPrefix()+"robot";
    // 密码规则正则表达式
    public static final String REGEX_PASSWORD = "^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,18}$";
}
