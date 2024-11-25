package cn.havaachat.service.impl;

import cn.havaachat.config.AppConfiguration;
import cn.havaachat.constants.AccountConstants;
import cn.havaachat.constants.RedisConstants;
import cn.havaachat.enums.*;
import cn.havaachat.exception.AccountException;
import cn.havaachat.mapper.UserContactMapper;
import cn.havaachat.mapper.UserInfoBeautyMapper;
import cn.havaachat.mapper.UserInfoMapper;
import cn.havaachat.pojo.dto.LoginDTO;
import cn.havaachat.pojo.dto.RegisterDTO;
import cn.havaachat.pojo.dto.TokenUserInfoDTO;
import cn.havaachat.pojo.entity.UserContact;
import cn.havaachat.pojo.entity.UserInfo;
import cn.havaachat.pojo.entity.UserInfoBeauty;
import cn.havaachat.pojo.vo.UserInfoVO;
import cn.havaachat.redis.RedisService;
import cn.havaachat.redis.RedisUtils;
import cn.havaachat.service.AccountService;
import cn.havaachat.service.UserContactService;
import cn.havaachat.utils.StringUtils;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {
    private RedisUtils redisUtils;
    private RedisService redisService;
    private UserInfoMapper userInfoMapper;
    private UserInfoBeautyMapper userInfoBeautyMapper;
    private UserContactMapper userContactMapper;
    private AppConfiguration appConfiguration;
    private UserContactService userContactService;
    @Autowired
    public AccountServiceImpl(RedisUtils redisUtils,UserInfoMapper userInfoMapper,UserInfoBeautyMapper userInfoBeautyMapper,
                              AppConfiguration appConfiguration,RedisService redisService,UserContactMapper userContactMapper,
                              UserContactService userContactService){
        this.redisUtils = redisUtils;
        this.userInfoMapper = userInfoMapper;
        this.userInfoBeautyMapper = userInfoBeautyMapper;
        this.appConfiguration = appConfiguration;
        this.redisService = redisService;
        this.userContactMapper = userContactMapper;
        this.userContactService = userContactService;
    }

    /**
     * 生成验证码
     * @return
     */
    public Map<String,String> generateCheckCode(){
        log.info("生成验证码");
        // 生成一个验证码
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        // 验证码的正确结果
        String checkCodeAnswer = captcha.text();
        // 该验证码的唯一标识，后端存checkCodeKey:checkCodeAnswer，前端传回checkCodeKey和输入的Answer，与后端校验
        String checkCodeKey = UUID.randomUUID().toString();
        // 将验证码编成Base64返回
        String checkCode = captcha.toBase64();
        Map<String,String> result = new HashMap<>();
        result.put("checkCode",checkCode);
        result.put("checkCodeKey",checkCodeKey);
        // redis存储，验证码过期时间为10min
        redisUtils.set(RedisConstants.REDIS_KEY_CHECKCODE+checkCodeKey,checkCodeAnswer,60*10);
        return result;
    }

    /**
     * 校验验证码并注册
     * @param registerDTO
     */
    @Transactional
    public void checkCheckCodeAndRegister(RegisterDTO registerDTO){
        // 校验验证码
        checkCheckCode(registerDTO.getCheckCodeKey(),registerDTO.getCheckCode());
        // 注册
        register(registerDTO);
    }
    /**
     * 校验验证码并登录
     * @param loginDTO
     */
    @Transactional
    public UserInfoVO checkCheckCodeAndLogin(LoginDTO loginDTO){
        // 校验验证码
        checkCheckCode(loginDTO.getCheckCodeKey(),loginDTO.getCheckCode());
        // 登录
        return login(loginDTO);
    }
    /**
     * 验证码校验
     * @param checkCodeKey
     * @param checkCodeAnswer
     */
    public void checkCheckCode(String checkCodeKey,String checkCodeAnswer){
        log.info("校验验证码");
        try{
            // 验证码校验
            if(!checkCodeAnswer.equals(redisUtils.get(StringUtils.getRedisCheckcodeKey(checkCodeKey)))){
                throw new AccountException(ResponseCodeEnum.CODE_400.getCode(), AccountConstants.CHECKCODE_WRONG);
            }
        }finally {
            // 无论成功与否，本次校验都要删除验证码
            redisUtils.del(StringUtils.getRedisCheckcodeKey(checkCodeKey));
            log.info("验证码删除成功");
        }
    }
    /**
     * 注册
     * 前端调注册接口传未md5加密的密码，调登录接口传已md5加密的密码
     * @param registerDTO
     * @return
     */
    @Transactional
    public void register(RegisterDTO registerDTO){
        log.info("注册:{}",registerDTO);
        String nickName = registerDTO.getNickName();
        String email = registerDTO.getEmail();
        String password = registerDTO.getPassword();

        // 用户已存在
        UserInfo existUserInfo = userInfoMapper.findByEmail(email);
        if(null!=existUserInfo){
            throw new AccountException(ResponseCodeEnum.CODE_400.getCode(),AccountConstants.REGISTER_USER_EXISTED);
        }

        UserInfo userInfo = new UserInfo();
        // 为该用户分配随机id
        userInfo.setUserId(StringUtils.generateRandomUserId());
        userInfo.setNickName(nickName);
        userInfo.setEmail(email);
        // 密码存md5加密形式
        userInfo.setPassword(StringUtils.transferStringToMd5(password));
        userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
        // 审核好友类型
        userInfo.setJoinType(JoinTypeEnum.APPLY.getType());
        Long now = System.currentTimeMillis();
        // 设置最后离线时间
        userInfo.setLastOffTime(now);
        // 是否为该用户分配靓号id
        UserInfoBeauty existUserInfoBeauty = userInfoBeautyMapper.findByEmail(email);
        if(null!=existUserInfoBeauty&& BeautyAccountStatusEnum.NO_USE.getStatus().equals(existUserInfoBeauty.getStatus())){
            userInfo.setUserId(StringUtils.spliceUserId(existUserInfoBeauty.getUserId()));
            existUserInfoBeauty.setStatus(BeautyAccountStatusEnum.USED.getStatus());
            userInfoBeautyMapper.update(existUserInfoBeauty);
        }
        // 插入数据库
        userInfoMapper.insert(userInfo);
        // 创建机器人好友
        userContactService.addContact4Robot(userInfo.getUserId());
    }

    /**
     * 登录
     * 前端调注册接口传未md5加密的密码，调登录接口传已md5加密的密码
     * @param loginDTO
     * @return
     */
    @Transactional
    public UserInfoVO login(LoginDTO loginDTO){
        log.info("登录：{}",loginDTO);
        String email = loginDTO.getEmail();
        // 前端传过来的密码已md5加密，无需再加密
        String password = loginDTO.getPassword();
        // 判断账号密码是否正确
        UserInfo existUserInfo = userInfoMapper.findByEmail(email);
        if(null==existUserInfo || !password.equals(existUserInfo.getPassword())){
            throw new AccountException(ResponseCodeEnum.CODE_400.getCode(),AccountConstants.LOGIN_WRONG);
        }
        // 判断账户是否被禁用
        if(UserStatusEnum.DISABLE.getStatus().equals(existUserInfo.getStatus())){
            throw new AccountException(ResponseCodeEnum.CODE_400.getCode(),AccountConstants.LOGIN_USER_DISABLE);
        }
        // 判断账户是否已登录
        Long lastUserHeartBeat = redisService.getUserHeartBeat(existUserInfo.getUserId());
        // 若redis中用户心跳未过期，说明用户已在其他地方登录
        if(null!=lastUserHeartBeat){
            throw new AccountException(ResponseCodeEnum.CODE_400.getCode(),AccountConstants.LOGIN_USER_LOGIN_ALREADY);
        }

        // 查询用户所有有效联系人，并存入redis
        List<UserContact> userContactList = userContactMapper.findBatchByUserIdAndStatus(existUserInfo.getUserId(), UserContactStatusEnum.FRIEND.getStatus());
        if (!userContactList.isEmpty()){
            List<String> userContactIdList = userContactList.stream().map(item->item.getContactId()).collect(Collectors.toList());
            // 先清理，再加入
            redisService.cleanUserContact(existUserInfo.getUserId());
            redisService.saveUserContactIdList(existUserInfo.getUserId(), userContactIdList);
        }

        // 生成token信息
        TokenUserInfoDTO tokenUserInfo = getTokenUserInfo(existUserInfo);
        // 拼装UserInfoVO对象返回
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(existUserInfo,userInfoVO);
        userInfoVO.setToken(tokenUserInfo.getToken());
        userInfoVO.setAdmin(tokenUserInfo.getAdmin());

        // 将token信息存入redis
        redisService.saveTokenUserInfoDTOAndToken(tokenUserInfo);
        return userInfoVO;
    }
    /**
     * 获取用户token信息
     * @param userInfo
     * @return
     */
    public TokenUserInfoDTO getTokenUserInfo(UserInfo userInfo){
        TokenUserInfoDTO tokenUserInfoDTO = new TokenUserInfoDTO();
        tokenUserInfoDTO.setUserId(userInfo.getUserId());
        tokenUserInfoDTO.setNickName(userInfo.getNickName());
        boolean isAdmin = ArrayUtils.contains(appConfiguration.getAdminEmails().split(","),userInfo.getEmail());
        tokenUserInfoDTO.setAdmin(isAdmin);
        // 生成token
        tokenUserInfoDTO.setToken(StringUtils.generateUserToken(userInfo.getUserId()));
        log.info("生成用户token信息：{}",tokenUserInfoDTO);
        return tokenUserInfoDTO;
    }
}
