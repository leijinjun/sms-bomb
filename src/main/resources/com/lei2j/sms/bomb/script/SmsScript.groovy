package com.lei2j.sms.bomb.script

import com.lei2j.sms.bomb.entity.SmsUrlConfig
import groovy.json.JsonSlurper
import groovy.xml.XmlSlurper

trait SmsScript {

    enum ResponseTypeEnum {
        JSON,
        XML,
        JSONP,
        TEXT;
    }

    void preProcess(SmsUrlConfig smsUrlConfig, Map<String, String> paramsMap, Map<String, String> headerMap) {}

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
            def keyPair = sp[0]
            def valuePair = sp[1]
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