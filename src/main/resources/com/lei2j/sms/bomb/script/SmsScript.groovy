package com.lei2j.sms.bomb.script

import com.alibaba.fastjson.JSONObject
import com.lei2j.sms.bomb.entity.SmsUrlConfig
import groovy.json.JsonSlurper
import groovy.xml.XmlSlurper
import org.apache.commons.lang3.StringUtils

trait SmsScript {

    enum ResponseTypeEnum {
        JSON,
        XML,
        JSONP,
        TEXT;
    }

    void preProcess(SmsUrlConfig smsUrlConfig, Map<String, Object> paramsMap, Map<String, String> headerMap) {
        List<String> headerList = smsUrlConfig.getHeaderList()
        if (!headerMap.containsKey("Accept")) {
            headerMap.put("Accept", "*/*")
        }else if (!headerMap.containsKey("accept")) {
            headerMap.put("Accept", "*/*")
        }
        headerList.forEach((headerPair) -> {
            String[] split = headerPair.split(":", 2)
            String name = split[0];
            String value = split[1];
            if (StringUtils.isNotBlank(value)) {
                if ("cookie".equalsIgnoreCase(name)) {
                    String o1 = headerMap.computeIfPresent("Cookie",(key,val)->{(val + (val.endsWith(";") ? "" : ";") + value)})
                    String o2 = headerMap.computeIfPresent("cookie", (key, val) -> (val + (val.endsWith(";") ? "" : ";") + value))
                    if (o1 == null && o2 == null) {
                        headerMap.put("Cookie", value)
                    }
                } else {
                    headerMap.put(name, value)
                }
            }
        })
        //解析固定请求参数
        String bindingParams = smsUrlConfig.getBindingParams()
        if (StringUtils.isNotBlank(bindingParams)) {
            JSONObject object = JSONObject.parseObject(bindingParams)
            for (Map.Entry<String, Object> entry : object.entrySet()) {
                paramsMap.put(entry.getKey(), entry.getValue())
            }
        }
    }

    Boolean postProcess(SmsUrlConfig smsUrlConfig, String response) {
        if (response == null || response.isEmpty()) {
            return Boolean.TRUE
        }
        def successCode = smsUrlConfig.getSuccessCode()
        if (successCode == null || successCode.isEmpty()) {
            return Boolean.TRUE
        }
        def responseType = smsUrlConfig.getResponseType()
        boolean isXml = ResponseTypeEnum.XML.name().equalsIgnoreCase(responseType)
        boolean isJson = ResponseTypeEnum.JSON.name().equalsIgnoreCase(responseType)
        if (isXml || isJson) {
            def sp = successCode.split(",", 2)
            def keyPair = sp[0].trim()
            def valuePair = sp[1].trim()
            def code = keyPair.split("=", 2)[1]
            def value = valuePair.split("=", 2)[1]
            def split = code.split("\\.")
            def parseObject = isXml ? new XmlSlurper().parseText(response) : new JsonSlurper().parseText(response)
            for (int i = 0; i < split.length; i++) {
                parseObject = parseObject.(split[i])
            }
            parseObject = parseObject.toString()
            return parseObject == value
        }else if (ResponseTypeEnum.TEXT.name().equalsIgnoreCase(responseType)) {
            return Boolean.TRUE
        }
        throw new UnsupportedOperationException(responseType)
    }

    String parseJsonp(String callback, String jsonp, int paramNumber, int paramIndex) {
        def response = jsonp.substring(callback.length() + 1, jsonp.length() - 1)
        response.split(",", paramNumber)[paramIndex]
    }
}