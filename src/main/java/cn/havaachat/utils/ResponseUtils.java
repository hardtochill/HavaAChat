package cn.havaachat.utils;


import cn.havaachat.enums.ResponseCodeEnum;
import cn.havaachat.pojo.vo.ResponseVO;

import javax.xml.ws.Response;

/**
 * 响应数据(结果)最外层对象工具
 *
 */
public class ResponseUtils {
    /**
     * 操作成功
     *
     * @param msg    提示信息
     * @param object 对象
     */
    public static <T> ResponseVO<T> success(String msg, T object) {
        ResponseVO<T> responseVo = new ResponseVO<>();
        responseVo.setInfo(msg);
        responseVo.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVo.setData(object);
        return responseVo;
    }

    /**
     * 操作成功，使用默认的提示信息
     *
     * @param object 对象
     */
    public static <T> ResponseVO<T> success(T object) {
        String message = ResponseCodeEnum.CODE_200.getMsg();
        return success(message, object);
    }

    /**
     * 操作成功，返回提示信息，不返回数据
     */
    public static <T> ResponseVO<T> success(String msg) {
        return success(msg, null);
    }

    /**
     * 操作成功，不返回数据
     */
    public static ResponseVO success() {
        return success(null);
    }

    /**
     * 操作有误
     *
     * @param code 错误码
     * @param msg  提示信息
     */
    public static ResponseVO error(Integer code, String msg) {
        ResponseVO responseVo = new ResponseVO();
        responseVo.setInfo(msg);
        responseVo.setCode(code);
        return responseVo;
    }

    /**
     * 操作有误，使用默认400错误码
     *
     * @param msg 提示信息
     */
    public static ResponseVO error(String msg) {
        Integer code = ResponseCodeEnum.CODE_400.getCode();
        return error(code, msg);
    }

    /**
     * 操作有误，只返回默认错误状态码
     */
    public static ResponseVO error() {
        return error(null);
    }

}


