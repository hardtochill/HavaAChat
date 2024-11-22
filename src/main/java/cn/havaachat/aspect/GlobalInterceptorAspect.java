package cn.havaachat.aspect;

import cn.havaachat.annotation.GlobalInterceptor;
import cn.havaachat.context.BaseContext;
import cn.havaachat.enums.ResponseCodeEnum;
import cn.havaachat.exception.BaseException;
import cn.havaachat.pojo.dto.TokenUserInfoDTO;
import cn.havaachat.redis.RedisUtils;
import cn.havaachat.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 全局拦截切面
 * 用于登录权限校验
 */
@Aspect
@Component
@Slf4j
public class GlobalInterceptorAspect {
    private RedisUtils redisUtils;
    @Autowired
    public GlobalInterceptorAspect(RedisUtils redisUtils){
        this.redisUtils = redisUtils;
    }
    /**
     * 锁定到controller包下所有加了@GlobalInterceptor注解的方法
     */
    @Pointcut("execution(* cn.havaachat.controller..*.*(..))&&@annotation(cn.havaachat.annotation.GlobalInterceptor)")
    public void pointCut(){}

    @Before("pointCut()")
    public void intercept(JoinPoint joinPoint){
        try{
            // 获取方法对象
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            // 获取注解
            GlobalInterceptor globalInterceptorAnnotation = method.getAnnotation(GlobalInterceptor.class);
            if(null==globalInterceptorAnnotation){
                return;
            }
            // 根据注解看是否需要执行校验逻辑
            if(globalInterceptorAnnotation.checkLogin() || globalInterceptorAnnotation.checkAdmin()){
                checkLogin(globalInterceptorAnnotation.checkAdmin());
            }
        }catch (BaseException e){
            throw e;
        } catch (Exception e){
            log.error("权限校验拦截异常",e);
            throw new BaseException(ResponseCodeEnum.CODE_500);
        }
    }

    /**
     * 权限校验
     * @param checkAmin
     */
    public void checkLogin(Boolean checkAmin){
        // 得到当前线程的全局HttpServletRequest
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        // 拿到请求头的token
        String token = request.getHeader("token");
        // 未携带token
        if(StringUtils.isEmpty(token)){
            throw new BaseException(ResponseCodeEnum.CODE_901);
        }
        // 在redis中取出tokenUserInfo，进行权限校验
        TokenUserInfoDTO tokenUserInfoDTO = (TokenUserInfoDTO)redisUtils.get(StringUtils.getRedisTokenUserInfoKey(token));
        log.info("权限校验拦截，token：{}",tokenUserInfoDTO);
        // 未存储用户token信息
        if(null==tokenUserInfoDTO){
            throw new BaseException(ResponseCodeEnum.CODE_901);
        }
        if(checkAmin && !tokenUserInfoDTO.getAdmin()){
            log.error("用户不具备超级管理员权限: {}",tokenUserInfoDTO);
            throw new BaseException(ResponseCodeEnum.CODE_404);
        }
        // 将当前用户token存入ThreadLocal
        BaseContext.setTokenUserInfo(tokenUserInfoDTO);
    }
}
