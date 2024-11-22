package cn.havaachat.context;

import cn.havaachat.pojo.dto.TokenUserInfoDTO;

/**
 * 上下文信息，用于同一线程上下文间通信
 */
public class BaseContext {
    /**
     * 在请求时拦截，存储用户Token信息
     */
    public static ThreadLocal<TokenUserInfoDTO> threadLocal = new ThreadLocal<>();
    public static void setTokenUserInfo(TokenUserInfoDTO tokenUserInfo){
        threadLocal.set(tokenUserInfo);
    }
    public static TokenUserInfoDTO getTokenUserInfo(){
        return threadLocal.get();
    }
    public static void removeTokenUserInfo(){
        threadLocal.remove();
    }
}
