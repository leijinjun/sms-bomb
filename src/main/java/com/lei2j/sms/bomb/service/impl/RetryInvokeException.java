package com.lei2j.sms.bomb.service.impl;

/**
 * 短信接口重试异常
 * @author leijinjun
 * @date 2023/4/24
 **/
public class RetryInvokeException extends RuntimeException {

    public RetryInvokeException(String message) {
        super(message);
    }

    public RetryInvokeException(String message, Throwable cause) {
        super(message, cause);
    }
}
