package script

import com.lei2j.sms.bomb.entity.SmsUrlConfig
import groovy.json.JsonSlurper
import groovy.xml.XmlSlurper

trait SmsScript {

    enum ResponseTypeEnum {
        JSON,
        XML,
        TEXT;
    }

    void preProcess(SmsUrlConfig smsBomb, Map<String, Object> paramsMap, Map<String, String> headerMap) {}

    Boolean postProcess(SmsUrlConfig smsBomb, String response) {
        if (response == null || response.isEmpty()) {
            return Boolean.TRUE
        }
        def successCode = smsBomb.getSuccessCode()
        if (successCode == null || successCode.isEmpty()) {
            return Boolean.TRUE
        }
        def responseType = smsBomb.getResponseType()
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
}