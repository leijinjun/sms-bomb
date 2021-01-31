package com.lei2j.sms.bomb.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lei2j.sms.bomb.repository.SmsSendLogRepository;
import com.lei2j.sms.bomb.repository.SmsUrlConfigRepository;
import com.lei2j.sms.bomb.util.HttpUtils;
import com.lei2j.sms.bomb.dto.SmsSendDTO;
import com.lei2j.sms.bomb.entity.SmsUrlConfig;
import com.lei2j.sms.bomb.entity.SmsSendLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author leijinjun
 * @date 2020/11/29
 **/
@Service
public class SmsSendService extends CommonServiceImpl {
    private static final Map<String, String> FIXED_HEADER = new LinkedHashMap<>();

    static{
        FIXED_HEADER.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, " + "like Gecko) Chrome/87.0.4280.67 Safari/537.36 Edg/87.0.664.47");
        FIXED_HEADER.put("Connection", "close");
    }

    private final int coreSize = Runtime.getRuntime().availableProcessors() + 1;

    private final ThreadPoolExecutor executorService =
            new ThreadPoolExecutor(coreSize, 2 * coreSize + 1, 15, TimeUnit.MINUTES, new ArrayBlockingQueue<>(2000), new ThreadPoolExecutor.DiscardOldestPolicy());

    private final ThreadPoolExecutor logExecutorService =
            new ThreadPoolExecutor(2, 5, 15, TimeUnit.MINUTES, new ArrayBlockingQueue<>(2000), new ThreadPoolExecutor.DiscardOldestPolicy());

    private final GroovySmsScriptExecutorService groovyScriptExecutorService;

    private final SmsSendLogRepository smsSendLogRepository;

    private final SmsUrlConfigRepository smsUrlConfigRepository;

    @Value("${smb.bomb.send.window.size}")
    private Integer sendSize;

    public SmsSendService(SmsUrlConfigRepository smsUrlConfigRepository, GroovySmsScriptExecutorService groovyScriptExecutorService,
                          SmsSendLogRepository smsSendLogRepository) {
        this.smsUrlConfigRepository = smsUrlConfigRepository;
        this.groovyScriptExecutorService = groovyScriptExecutorService;
        this.smsSendLogRepository = smsSendLogRepository;
    }

    public void send(SmsSendDTO smsSendDTO) {
        Integer windowSize = sendSize;
        Integer count = smsUrlConfigRepository.countByNormalEquals(Boolean.TRUE);
        if (windowSize > count) {
            windowSize = count;
        }
        List<SmsUrlConfig> list = smsUrlConfigRepository.findTopListByNormalEquals(Boolean.TRUE, windowSize);
        if (!CollectionUtils.isEmpty(list)) {
            for (SmsUrlConfig entity : list) {
                executorService.submit(() -> {
                    Map<String, String> headerMap = new LinkedHashMap<>(6);
                    Map<String,Object> paramsMap = new LinkedHashMap<>(6);
                    int duration = -1;
                    Boolean success = Boolean.FALSE;
                    String response = null;
                    try {
                        headerMap.putAll(FIXED_HEADER);
                        paramsMap.put(entity.getPhoneParamName(), smsSendDTO.getPhone());
                        groovyScriptExecutorService.preInvoke(entity, paramsMap, headerMap);
                        logger.info("[smb.send]url:{},params:{},headers:{}", entity.getSmsUrl(), paramsMap, headerMap);
                        long startTime = System.currentTimeMillis();
                        response = request(entity, paramsMap, headerMap);
                        long endTime = System.currentTimeMillis();
                        duration = (int) (endTime - startTime);
                        logger.info("[sms.send]response:{},requestTime:{}ms", response, duration);
                        //解析响应
                        success = (Boolean) groovyScriptExecutorService.postInvoke(entity, response);
                    } catch (HttpResponseException e) {
                        response = String.format("{\"httpStatusCode\":\"%s\",\"reason\":\"%s\"}", e.getStatusCode(), e.getReasonPhrase());
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        saveAsyncSendLog(smsSendDTO.getPhone(), smsSendDTO.getClientIp(), entity.getSmsUrl(), paramsMap, response, duration, success);
                    }
                });
            }
        }
    }

    private String request(SmsUrlConfig smsUrlConfig, Map<String, Object> paramsMap, Map<String, String> headerMap) throws IOException {
        List<String> headerList = smsUrlConfig.getHeaderList();
        headerList.forEach((headerPair) -> {
            String[] split = headerPair.split(":", 2);
            String name = split[0];
            String value = split[1];
            if (StringUtils.isNotBlank(value)) {
                if ("cookie".equalsIgnoreCase(name)) {
                    String o1 = headerMap.computeIfPresent("Cookie", (key, val) -> (val + (val.endsWith(";") ? "" : ";") + value));
                    String o2 = headerMap.computeIfPresent("cookie", (key, val) -> (val + (val.endsWith(";") ? "" : ";") + value));
                    if (o1 == null && o2 == null) {
                        headerMap.put("Cookie", value);
                    }
                } else {
                    headerMap.put(name, value);
                }
            }
        });
        //解析固定请求参数
        String bindingParams = smsUrlConfig.getBindingParams();
        if (StringUtils.isNotBlank(bindingParams)) {
            JSONObject object = JSONObject.parseObject(bindingParams);
            for (Map.Entry<String, Object> entry : object.entrySet()) {
                paramsMap.put(entry.getKey(), entry.getValue());
            }
        }
        String requestMethod = smsUrlConfig.getRequestMethod().toUpperCase();
        String smsUrl = smsUrlConfig.getSmsUrl();
        if (HttpMethod.GET.matches(requestMethod)) {
            return HttpUtils.get(smsUrl, paramsMap, headerMap);
        } else if (HttpMethod.POST.matches(requestMethod)) {
            String contentType = smsUrlConfig.getContentType();
            if ("application/json".equalsIgnoreCase(contentType)) {
                return HttpUtils.post(smsUrl, JSON.toJSONString(paramsMap), headerMap);
            } else if ("application/x-www-form-urlencoded".equalsIgnoreCase(contentType)) {
                return HttpUtils.postFormUrlencoded(smsUrl, paramsMap, headerMap);
            } else {
                throw new UnsupportedOperationException(contentType);
            }
        } else {
            throw new UnsupportedOperationException(requestMethod);
        }
    }

    private void saveAsyncSendLog(String phone, String ip, String smsUrl, Object params, String response, int requestDuration, boolean success) {
        logExecutorService.execute(() -> {
            SmsSendLog record = new SmsSendLog();
            record.setPhone(phone);
            record.setIp(ip);
            record.setSmsUrl(smsUrl);
            record.setParams(JSONObject.toJSONString(params));
            record.setResponse(response);
            record.setRequestDuration(requestDuration);
            record.setResponseStatus(success ? SmsSendLog.SUCCESS_STATUS : SmsSendLog.FAILURE_STATUS);
            smsSendLogRepository.save(record);
        });
    }
}

