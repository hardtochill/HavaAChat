package cn.havaachat.aspect;

import cn.havaachat.constants.PageQueryAutoFillConstants;
import cn.havaachat.enums.ResponseCodeEnum;
import cn.havaachat.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 分页查询填充切面类
 */
@Aspect
@Component
@Slf4j
public class PageQueryAutoFillAspect {
    /**
     * 锁定到controller包下，所有加了@PageQueryAutoFill注解的方法
     */
    @Pointcut("execution(* cn.havaachat.controller..*.*(..))&&@annotation(cn.havaachat.annotation.PageQueryAutoFill)")
    public void pointCut(){}

    @Before("pointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("分页查询参数自动校验填充");
        // 获取PageDTO对象
        Object pageDTO = joinPoint.getArgs()[0];
        try{
            Field pageNo = pageDTO.getClass().getDeclaredField(PageQueryAutoFillConstants.PAGE_NO);
            Field pageSize = pageDTO.getClass().getDeclaredField(PageQueryAutoFillConstants.PAGE_SIZE);
            // 由于都为私有变量，因此要设置临时访问权限
            pageNo.setAccessible(true);
            pageSize.setAccessible(true);
            // 获取pageDTO的这两个属性值，若为null则填充默认值
            if(null==pageNo.get(pageDTO)){
                pageNo.set(pageDTO,PageQueryAutoFillConstants.PAGE_NO_DEFAULT);
            }
            if(null==pageSize.get(pageDTO)){
                pageSize.set(pageDTO,PageQueryAutoFillConstants.PAGE_SIZE_DEFAULT);
            }
        }catch (Exception e){
            log.error("分页查询公共字段填充异常",e);
            throw new BaseException(ResponseCodeEnum.CODE_500);
        }
    }
}
