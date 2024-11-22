package cn.havaachat.handler;

import cn.havaachat.enums.ResponseCodeEnum;
import cn.havaachat.exception.BaseException;
import cn.havaachat.pojo.vo.ResponseVO;
import cn.havaachat.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 业务异常
     * @param baseException
     * @return
     */
    @ExceptionHandler
    public ResponseVO baseExceptionHandler(BaseException baseException){
      log.error("异常信息",baseException);
      return ResponseUtils.error(baseException.getCode(),baseException.getMessage());
    }

    /**
     * JSR303入参校验异常：表单——多个对象一一接收
     * @param constraintViolationException
     * @return
     */
    @ExceptionHandler
    public ResponseVO constraintViolationExceptionHandler(ConstraintViolationException constraintViolationException){
        log.error("异常信息",constraintViolationException);
        return ResponseUtils.error(ResponseCodeEnum.CODE_600.getCode(),ResponseCodeEnum.CODE_600.getMsg());
    }
    /**
     * JSR303入参校验异常：表单——一个封装对象接收
     * @param bindException
     * @return
     */
    @ExceptionHandler
    public ResponseVO bindExceptionHandler(BindException bindException){
        log.error("异常信息：",bindException);
        return ResponseUtils.error(ResponseCodeEnum.CODE_600.getCode(),ResponseCodeEnum.CODE_600.getMsg());
    }

    /**
     * 其它异常
     * @param e
     * @return
     */
    @ExceptionHandler
    public ResponseVO exceptionHandler(Exception e){
        log.error("异常信息",e);
        return ResponseUtils.error(ResponseCodeEnum.CODE_500.getCode(),ResponseCodeEnum.CODE_500.getMsg());
    }
}
