package com.lei2j.sms.bomb.service.impl;

/**
 * 该异常标志取消发送
 * @author leijinjun
 * @date 2023/5/13
 **/
public class CancelException extends RuntimeException {

    public CancelException(String message) {
        super(message);
    }

    public CancelException(String message, Throwable cause) {
        super(message, cause);
    }
}
