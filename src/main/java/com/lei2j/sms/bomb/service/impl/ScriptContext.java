package com.lei2j.sms.bomb.service.impl;

import com.lei2j.sms.bomb.entity.SmsUrlConfig;
import com.lei2j.sms.bomb.util.SpringApplicationUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author leijinjun
 * @date 2023/1/9
 **/
public class ScriptContext {

    private SmsUrlConfig smsUrlConfig;

    private final Map<String,Object> paramsMap = new HashMap<>();

    private final Map<String, String> headerMap = new HashMap<>();

    private final Map<String, String> queryMap = new HashMap<>();

    /**
     *
     */
    private final Map<String, Object> contextDataMap = new HashMap<>();

    private String response;

    private transient SpringApplicationUtils springApplicationUtils;

    /**
     * 上一次脚本上下文内容
     */
    private transient ScriptContext preScriptContext;

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

    public Map<String, Object> getContextDataMap() {
        return contextDataMap;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    /**
     * @return 手机号参数名称
     */
    public final String getPhoneParamName(){
        return Optional.ofNullable(smsUrlConfig).map(SmsUrlConfig::getPhoneParamName).orElse(null);
    }

    /**
     * @return 手机号
     */
    public final String getPhoneValue(){
        return paramsMap.get(getPhoneParamName()).toString();
    }

    public void cloneFromPre() {
        if (Objects.nonNull(preScriptContext)) {
            return;
        }
        getParamsMap().putAll(preScriptContext.getParamsMap());
        getQueryMap().putAll(preScriptContext.getQueryMap());
        getHeaderMap().putAll(preScriptContext.getHeaderMap());
        getContextDataMap().putAll(preScriptContext.getContextDataMap());
    }

    public SpringApplicationUtils getSpringApplicationUtils() {
        return springApplicationUtils;
    }

    public void setSpringApplicationUtils(SpringApplicationUtils springApplicationUtils) {
        this.springApplicationUtils = springApplicationUtils;
    }

    public ScriptContext getPreScriptContext() {
        return preScriptContext;
    }

    public void setPreScriptContext(ScriptContext preScriptContext) {
        this.preScriptContext = preScriptContext;
    }

    public Object setParamsEntry(String key, Object value) {
        return getParamsMap().put(key, value);
    }
}
