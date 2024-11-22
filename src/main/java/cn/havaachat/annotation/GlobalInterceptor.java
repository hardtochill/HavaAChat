package cn.havaachat.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 全局拦截注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalInterceptor {
    /**
     * 是否要检验登录
     * @return
     */
    boolean checkLogin() default true;

    /**
     * 是否要检验是否为超级管理员
     * @return
     */
    boolean checkAdmin() default false;
}
