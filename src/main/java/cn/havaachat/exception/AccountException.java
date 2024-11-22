package cn.havaachat.exception;


public class AccountException extends BaseException{
    public AccountException(){};
    public AccountException(Integer code,String msg){
        super(code,msg);
    }
}
