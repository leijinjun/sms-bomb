package com.lei2j.sms.bomb.script

import com.alibaba.fastjson.JSONObject
import com.lei2j.sms.bomb.entity.SmsUrlConfig
import groovy.json.JsonSlurper
import groovy.xml.XmlSlurper
import org.apache.commons.lang3.StringUtils
import org.apache.http.HeaderElement
import org.apache.http.message.BasicHeaderElement

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
            String name = split[0]
            String value = split[1]
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

    @Deprecated
    Boolean postProcess(SmsUrlConfig smsUrlConfig, String response) {
        return this.postProcess(smsUrlConfig, Collections.emptyMap(), Collections.emptyMap(), response)
    }

    Boolean postProcess(SmsUrlConfig smsUrlConfig, Map<String, Object> paramsMap, Map<String, String> headerMap, String response) {
        if (response == null || response.isEmpty()) {
            return Boolean.TRUE
        }
        def successCode = smsUrlConfig.getSuccessCode()
        if (successCode == null || successCode.isEmpty()) {
            return Boolean.TRUE
        }
        def responseType = smsUrlConfig.getResponseType()
        try {
            return parseResponse(smsUrlConfig, smsUrlConfig.getSuccessCode(), paramsMap, headerMap, response, ResponseTypeEnum.valueOf(responseType.toUpperCase()))
        } catch (Exception e) {
            e.printStackTrace()
            return false
        }
    }

    Boolean parseResponse(SmsUrlConfig smsUrlConfig, String resultFormatCode, Map<String, Object> paramsMap, Map<String, String> headerMap, String response, ResponseTypeEnum responseType) {
        def sp = resultFormatCode.split(",", 2)
        def keyPair = sp[0].trim()
        def valuePair = sp[1].trim()
        def code = keyPair.split("=", 2)[1]
        def value = valuePair.split("=", 2)[1]
        def split = code.split("\\.")
        def parseObject = null
        if (responseType == ResponseTypeEnum.XML) {
            parseObject = new XmlSlurper().parseText(response)
        } else if (responseType == ResponseTypeEnum.JSON) {
            parseObject = new JsonSlurper().parseText(response)
        } else if (responseType == ResponseTypeEnum.TEXT) {
            return parseText(smsUrlConfig, paramsMap, headerMap, response)
        } else if (responseType == ResponseTypeEnum.JSONP) {
            return parseJsonp(smsUrlConfig, paramsMap, headerMap, response)
        }
        if (Objects.nonNull(parseObject)) {
            for (int i = 0; i < split.length; i++) {
                parseObject = parseObject.(split[i].trim())
            }
            parseObject = parseObject.toString()
            return parseObject == value
        }
        return false
    }

    void retry(SmsUrlConfig smsUrlConfig, Map<String, Object> paramsMap, Map<String, String> headerMap, String response) {
        if (smsUrlConfig.getEndCode()) {
            String[] split = smsUrlConfig.getEndCode().split('[\r\n]')
            for (String sp : split) {
                if (parseResponse(smsUrlConfig, sp.trim(), paramsMap, headerMap, response, ResponseTypeEnum.valueOf(smsUrlConfig.getResponseType().toUpperCase()))) {
                    preProcess(smsUrlConfig, paramsMap, headerMap)
                    break
                }
            }
        }
    }

    /**
     *  由子类重写
     * @param smsUrlConfig
     * @param paramsMap
     * @param headerMap
     * @param response
     * @return
     */
    Boolean parseText(SmsUrlConfig smsUrlConfig, Map<String, Object> paramsMap, Map<String, String> headerMap, String response) {
        if (!response) {
            return true
        }
        response == smsUrlConfig.getSuccessCode()
    }

    /**
     *  由子类重写
     * @param smsUrlConfig
     * @param paramsMap
     * @param headerMap
     * @param response
     * @return
     */
    Boolean parseJsonp(SmsUrlConfig smsUrlConfig, Map<String, Object> paramsMap, Map<String, String> headerMap, String response) {
        false
    }

    void setCookie(String key, String value, Map<String, String> headerMap) {
        Map<String, List<HeaderElement[]>> responseHeaderMap = new HashMap<>()
        def list = new ArrayList<HeaderElement[]>()
        list.add(new BasicHeaderElement[]{new BasicHeaderElement(key, value)})
        responseHeaderMap.put('Set-Cookie', list)
        setCookie(responseHeaderMap, headerMap)
    }

    void setCookie(Map<String, List<HeaderElement[]>> responseHeaderMap, Map<String, String> headerMap) {
        setCookie(responseHeaderMap, headerMap, false)
    }

    void setCookie(Map<String, List<HeaderElement[]>> responseHeaderMap, Map<String, String> headerMap, boolean ifPresentReserve) {
        def headerElements = responseHeaderMap.get('Set-Cookie')?responseHeaderMap.get('Set-Cookie'):responseHeaderMap.get('set-cookie')
        if (headerElements) {
            headerElements.stream().forEach(c -> {
                String cookie = headerMap.get('Cookie')
                if (cookie) {
                    List<String> list = new ArrayList<>(Arrays.asList(cookie.split(';')))
                    boolean exist = list.stream().anyMatch(p -> {
                        String[] itemSplit = p.split('=', 2)
                        return Objects.equals(itemSplit[0].trim(), c[0].getName())
                    })
                    if (!(exist && ifPresentReserve)) {
                        list.removeIf(p -> p.trim().startsWith(c[0].getName().trim()))
                        list.add(c[0].getName() + '=' + c[0].getValue())
                    }
                    headerMap.put('Cookie', String.join(';', list))
                } else {
                    headerMap.put('Cookie', String.format('%s=%s', c[0].getName(), c[0].getValue()))
                }
            })
        }
    }


}