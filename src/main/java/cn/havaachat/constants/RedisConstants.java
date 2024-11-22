package cn.havaachat.constants;

public class RedisConstants {
    // 验证码在redis中存储的key
    public static final String REDIS_KEY_CHECKCODE = "havaachat:checkcode:";
    // 用户心跳在redis中存储的key
    public static final String REDIS_KEY_WS_USER_HEARTBEAT = "havaachat:ws:user:heartbeat:";
    // 用户token信息在redis中存储的key
    public static final String REDIS_KEY_WS_TOKEN_USERINFO = "havaachat:ws:token:";
    // 用户token在redis中的有效时间，为2天
    public static final Integer REDIS_KEY_EXPIRES_TOKEN = 60*60*24*2;
    // 用户userId在redis中存储的key
    public static final String REDIS_KEY_WS_TOKEN_USERINFO_USERID = "havaachat:ws:token:userid:";
    // 系统设置在redis中存储的key
    public static final String REDIS_KEY_SYE_SETTING = "havaachat:syssetting";
    // 用户心跳在redis中的存储时间
    public static final Integer REDIS_KEY_EXPIRES_HEART_BEAT = 6;
    // 用户联系人列表
    public static final String REDIS_KEY_USER_CONTACT = "havaachat:ws:user:contact:";
}
