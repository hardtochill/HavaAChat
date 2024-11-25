package cn.havaachat.utils;

import cn.havaachat.constants.FileConstants;
import cn.havaachat.constants.RedisConstants;
import cn.havaachat.enums.UserContactTypeEnum;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Arrays;

public class StringUtils extends org.apache.commons.lang3.StringUtils {
    /**
     * 生成长度为length的只含数字的字符串
     * @param length
     * @return
     */
    public static String generateRandomStringOnlyNumber(int length){
        return RandomStringUtils.random(length,false,true);
    }
    /**
     * 生成长度为length的含字母、数字的字符串
     * @param length
     * @return
     */
    public static String generateRandomString(int length){
        return RandomStringUtils.random(length,true,true);
    }
    /**
     * 生成随机用户id
     * 账号id长12位，第一位为用户标识，后11位为随机数
     * @return
     */
    public static String generateRandomUserId(){
        StringBuilder stringBuilder = new StringBuilder(UserContactTypeEnum.USER.getPrefix());
        return stringBuilder.append(generateRandomStringOnlyNumber(11)).toString();
    }
    /**
     * 生成随机群组id
     * 账号id长12位，第一位为群组标识，后11位为随机数
     * @return
     */
    public static String generateRandomGroupId(){
        StringBuilder stringBuilder = new StringBuilder(UserContactTypeEnum.GROUP.getPrefix());
        return stringBuilder.append(generateRandomStringOnlyNumber(11)).toString();
    }

    /**
     * 生成用户token
     * token:md5格式（用户id+20位随机字符串）
     * @param userId
     * @return
     */
    public static String generateUserToken(String userId){
        StringBuilder stringBuilder = new StringBuilder(userId);
        return transferStringToMd5(stringBuilder.append(generateRandomString(20)).toString());
    }
    /**
     * 拼接返回用户id
     * 输入为11位纯数字id，返回为加上用户前缀标识后的用户id
     * @param originId
     * @return
     */
    public static String spliceUserId(String originId){
        StringBuilder stringBuilder = new StringBuilder(UserContactTypeEnum.USER.getPrefix());
        return stringBuilder.append(originId).toString();
    }
    /**
     * 对字符串进行md5加密
     * @param originString
     * @return
     */
    public static String transferStringToMd5(String originString){
        return isEmpty(originString)?null: DigestUtils.md5Hex(originString);
    }

    /**
     * 返回验证码在redis中的key
     * @param checkcode
     * @return
     */
    public static String getRedisCheckcodeKey(String checkcode){
        StringBuilder stringBuilder = new StringBuilder(RedisConstants.REDIS_KEY_CHECKCODE);
        return stringBuilder.append(checkcode).toString();
    }

    /**
     * 返回TokenUserInfo在redis中存储的key
     * @param token
     * @return
     */
    public static String getRedisTokenUserInfoKey(String token){
        StringBuilder stringBuilder = new StringBuilder(RedisConstants.REDIS_KEY_WS_TOKEN_USERINFO);
        return stringBuilder.append(token).toString();
    }
    /**
     * 返回UserId在redis中存储的key
     * @param userId
     * @return
     */
    public static String getRedisTokenUserIdKey(String userId){
        StringBuilder stringBuilder = new StringBuilder(RedisConstants.REDIS_KEY_WS_TOKEN_USERINFO_USERID);
        return stringBuilder.append(userId).toString();
    }
    /**
     * 返回用户心跳在redis中的key
     * @param userId
     * @return
     */
    public static String getRedisWsUserHeartbeatKey(String userId){
        StringBuilder stringBuilder = new StringBuilder(RedisConstants.REDIS_KEY_WS_USER_HEARTBEAT);
        return stringBuilder.append(userId).toString();
    }

    /**
     * 版本更新文件名 APP_NAME_PREFIX.version.APP_NAME_SUFFIX
     * @param version
     * @return
     */
    public static String getAppUpdateFileName(String version){
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder.append(FileConstants.APP_NAME_PREFIX).append(version).append(FileConstants.APP_NAME_SUFFIX).toString();
    }

    /**
     * 解析出WebSocket连接的url中的token参数
     * @param url 格式为：path?token=xxx
     * @return
     */
    public static String getTokenInWebSocketUrl(String url){
        if(isEmpty(url) || !url.contains("?")){
            return null;
        }
        // urlAndParams[0]为path，urlAndParams[1]为携带的参数
        String[] pathAndParams = url.split("\\?");
        if (pathAndParams.length!=2){
            return null;
        }
        String[] tokenParams = pathAndParams[1].split("=");
        if (tokenParams.length!=2){
            return null;
        }
        return tokenParams[1];
    }

    /**
     * 获取用户在redis中存入的联系人id列表的key
     * @param userId
     * @return
     */
    public static String getRedisUserContactKey(String userId){
        StringBuilder stringBuilder = new StringBuilder(RedisConstants.REDIS_KEY_USER_CONTACT);
        return stringBuilder.append(userId).toString();
    }

    /**
     * 清除html标签相关，防html注入
     * @param content
     * @return
     */
    public static String cleanHtmlTag(String content){
        if (isEmpty(content)){
            return content;
        }
        content = content.replace("<","&lt;");
        content = content.replace("\r\n","<br>");
        content = content.replace("\n","<br>");
        return content;
    }

    /**
     * 为用户聊天双方生成唯一且不变的sessionId
     * @param userIdArray
     * @return
     */
    public static final String getChatSessionIdForUser(String[] userIdArray){
        // 将会话双方的id进行处理：排序+相连+md5
        Arrays.sort(userIdArray);
        return transferStringToMd5(join(userIdArray,""));
    }

    /**
     * 为群聊生成唯一且不变的sessionId
     * @param groupId
     * @return
     */
    public static final String getChatSessionIdForGroup(String groupId){
        return transferStringToMd5(groupId);
    }
}
