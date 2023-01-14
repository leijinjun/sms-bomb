package com.lei2j.sms.bomb.service.impl;

import com.lei2j.sms.bomb.entity.SmsUrlConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * @author leijinjun
 * @date 2023/1/9
 **/
public class ScriptContext {

    private SmsUrlConfig smsUrlConfig;

    private final Map<String,Object> paramsMap = new HashMap<>();

    private final Map<String, String> headerMap = new HashMap<>();

    private final Map<String, String> queryMap = new HashMap<>();

    private String response;

    public SmsUrlConfig getSmsUrlConfig() {
        return smsUrlConfig;
    }

    public void setSmsUrlConfig(SmsUrlConfig smsUrlConfig) {
        this.smsUrlConfig = smsUrlConfig;
    }

    public Map<String, Object> getParamsMap() {
        return paramsMap;
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    public Map<String, String> getQueryMap() {
        return queryMap;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
