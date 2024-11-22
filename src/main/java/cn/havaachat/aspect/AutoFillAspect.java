package cn.havaachat.aspect;

import cn.havaachat.annotation.AutoFill;
import cn.havaachat.constants.AutoFillConstants;
import cn.havaachat.enums.OperationTypeEnum;
import cn.havaachat.enums.ResponseCodeEnum;
import cn.havaachat.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 公共字段填充切面类
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 锁定到mapper包下，所有打了@AutoFill注解的方法
     */
    @Pointcut("execution(* cn.havaachat.mapper.*.*(..)) && @annotation(cn.havaachat.annotation.AutoFill)")
    public void pointcut(){}

    /**
     * 填充公共字段
     * @param joinPoint
     */
    @Before("pointcut()")
    public void autoFill(JoinPoint joinPoint){
        try{
            // 获取方法
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            Method method = methodSignature.getMethod();
            // 获取方法上的注解值，判断方法类型
            AutoFill autoFillAnnotation = method.getAnnotation(AutoFill.class);
            OperationTypeEnum operationType = autoFillAnnotation.value();
            // 获取方法形参表
            Object[] methodArgs = joinPoint.getArgs();
            if(null==methodArgs||0==methodArgs.length){
                return;
            }
            // 要填充的对象位于形参表首尾
            Object object = methodArgs[0];
            // 填充
            LocalDateTime nowLocalDateTime = LocalDateTime.now();
            if(OperationTypeEnum.INSERT==operationType){
                // 批量插入
                if(object instanceof List){
                    List<?> objectList = (List<?>)object;
                    for (Object o : objectList) {
                        Method setCreateTime = o.getClass().getMethod(AutoFillConstants.SET_CREATE_TIME, LocalDateTime.class);
                        Method setUpdateTime = o.getClass().getMethod(AutoFillConstants.SET_UPDATE_TIME, LocalDateTime.class);
                        setCreateTime.invoke(o,nowLocalDateTime);
                        setUpdateTime.invoke(o,nowLocalDateTime);
                    }
                }else{
                    // 单个插入
                    Method setCreateTime = object.getClass().getMethod(AutoFillConstants.SET_CREATE_TIME, LocalDateTime.class);
                    Method setUpdateTime = object.getClass().getMethod(AutoFillConstants.SET_UPDATE_TIME, LocalDateTime.class);
                    setCreateTime.invoke(object,nowLocalDateTime);
                    setUpdateTime.invoke(object,nowLocalDateTime);
                }
            }else{
                // 批量修改
                if(object instanceof List){
                    List<?> objectList = (List<?>)object;
                    for (Object o : objectList) {
                        Method setUpdateTime = o.getClass().getMethod(AutoFillConstants.SET_UPDATE_TIME, LocalDateTime.class);
                        setUpdateTime.invoke(o,nowLocalDateTime);
                    }
                }else{
                    // 单个修改
                    Method setUpdateTime = object.getClass().getMethod(AutoFillConstants.SET_UPDATE_TIME, LocalDateTime.class);
                    setUpdateTime.invoke(object,nowLocalDateTime);
                }
            }
        }catch (Exception e){
            log.error("增删操作公共字段填充异常",e);
            throw new BaseException(ResponseCodeEnum.CODE_500);
        }
    }
}
