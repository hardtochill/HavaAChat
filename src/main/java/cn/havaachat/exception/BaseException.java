package cn.havaachat.exception;

import cn.havaachat.enums.ResponseCodeEnum;
import lombok.Data;

/**
 * 异常类
 */
@Data
public class BaseException extends RuntimeException{
    private Integer code;
    private String msg;
    public BaseException(){}
    public BaseException(ResponseCodeEnum responseCodeEnum){
        this(responseCodeEnum.getCode(), responseCodeEnum.getMsg());
    }
    public BaseException(Integer code,String msg){
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
