package com.lei2j.sms.bomb.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lei2j.idgen.core.IdGenerator;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

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

    private final IdGenerator idGenerator;

    /**
     * 最大发送短信条数
     */
    @Value("${smb.bomb.send.window.size:3}")
    private Integer sendSize;

    /**
     * 发送短信失败时最大重试次数
     */
    @Value("${smb.bomb.send.retry.size:3}")
    private Integer maxRetryTimes;

    public SmsSendService(SmsUrlConfigRepository smsUrlConfigRepository,
                          GroovySmsScriptExecutorService groovyScriptExecutorService,
                          SmsSendLogRepository smsSendLogRepository,
                          IdGenerator idGenerator) {
        this.smsUrlConfigRepository = smsUrlConfigRepository;
        this.groovyScriptExecutorService = groovyScriptExecutorService;
        this.smsSendLogRepository = smsSendLogRepository;
        this.idGenerator = idGenerator;
    }

    public void send(SmsSendDTO smsSendDTO) {
        if (StringUtils.isBlank(smsSendDTO.getPhone())) {
            return;
        }
        Integer windowSize = smsSendDTO.getSendItems();
        Integer count = smsUrlConfigRepository.countByNormalEquals(Boolean.TRUE);
        if (windowSize > count) {
            windowSize = count;
        }
        List<SmsUrlConfig> list = smsUrlConfigRepository.findTopListByNormalEquals(Boolean.TRUE, windowSize);
        if (!CollectionUtils.isEmpty(list)) {
            for (SmsUrlConfig entity : list) {
                if (entity.getMaxRetryTimes() == null) {
                    entity.setMaxRetryTimes(maxRetryTimes);
                }else{
                    entity.setMaxRetryTimes(Math.min(maxRetryTimes, entity.getMaxRetryTimes()));
                }
                executorService.submit(new SmsTask(entity, smsSendDTO));
            }
        }
    }

    public void shutdown(String id) {
        if (executorService.isShutdown() || executorService.isTerminated()) {
            return;
        }
        final BlockingQueue<Runnable> queue = executorService.getQueue();
        queue.removeIf(p -> Objects.equals(id, ((SmsTask) p).smsSendDTO.getPhone()) || Objects.equals(id, ((SmsTask) p).smsSendDTO.getRequestId()));
    }

    public Boolean isFinished(String requestId) {
        final BlockingQueue<Runnable> queue = executorService.getQueue();
        final SmsSendDTO smsSendDTO = new SmsSendDTO();
        smsSendDTO.setRequestId(requestId);
        return !queue.contains(smsSendDTO);
    }

    private String request(ScriptContext scriptContext) throws IOException {
        SmsUrlConfig smsUrlConfig = scriptContext.getSmsUrlConfig();
        Map<String, Object> paramsMap = scriptContext.getParamsMap();
        Map<String, String> headerMap = scriptContext.getHeaderMap();
        Map<String, String> queryMap = scriptContext.getQueryMap();
        String requestMethod = smsUrlConfig.getRequestMethod().toUpperCase();
        String smsUrl = smsUrlConfig.getSmsUrl();
        if (HttpMethod.GET.matches(requestMethod)) {
            return HttpUtils.get(smsUrl, paramsMap, headerMap);
        } else if (HttpMethod.POST.matches(requestMethod)) {
            String contentType = smsUrlConfig.getContentType();
            if ("application/json".equalsIgnoreCase(contentType)) {
                return HttpUtils.post(smsUrl, JSON.toJSONString(paramsMap), queryMap, headerMap, null);
            } else if ("application/x-www-form-urlencoded".equalsIgnoreCase(contentType)) {
                return HttpUtils.postFormUrlencoded(smsUrl, paramsMap, headerMap, queryMap);
            } else {
                throw new UnsupportedOperationException(contentType);
            }
        } else {
            throw new UnsupportedOperationException(requestMethod);
        }
    }

    private void saveAsyncSendLog(SmsSendDTO smsSendDTO, SmsUrlConfig smsUrlConfig, Object params, String response,
                                  int requestDuration, boolean success) {
        logExecutorService.execute(() -> {
            SmsSendLog record = new SmsSendLog();
            record.setId((Long) idGenerator.next());
            record.setRequestId(smsSendDTO.getRequestId());
            record.setWebSiteName(smsUrlConfig.getWebsiteName());
            record.setPhone(Base64.getEncoder().encodeToString(smsSendDTO.getPhone().getBytes(StandardCharsets.UTF_8)));
            record.setIp(smsSendDTO.getClientIp());
            record.setSmsUrl(smsUrlConfig.getSmsUrl());
            record.setParams(JSONObject.toJSONString(params));
            record.setResponse(response);
            record.setRequestDuration(requestDuration);
            record.setResponseStatus(success ? SmsSendLog.SUCCESS_STATUS : SmsSendLog.FAILURE_STATUS);
            record.setCreateAt(LocalDateTime.now());
            smsSendLogRepository.save(record);
        });
    }

    private class SmsTask implements Runnable{

        private final SmsUrlConfig entity;

        private final SmsSendDTO smsSendDTO;

        public SmsTask(SmsUrlConfig entity, SmsSendDTO smsSendDTO) {
            this.entity = entity;
            this.smsSendDTO = smsSendDTO;
        }

        @Override
        public void run() {
            ScriptContext scriptContext = new ScriptContext();
            scriptContext.getHeaderMap().putAll(FIXED_HEADER);
            scriptContext.getParamsMap().put(entity.getPhoneParamName(), smsSendDTO.getPhone());
            scriptContext.getQueryMap().putAll(entity.getBindingQueryParamsMap());
            scriptContext.setSmsUrlConfig(entity);
            int duration = -1;
            Boolean success = Boolean.FALSE;
            String response = null;
            try {
                logger.info("[smb.send]url:{},params:{},headers:{}", entity.getSmsUrl(), scriptContext.getParamsMap(), scriptContext.getQueryMap());
                Integer maxRetryTimes = entity.getMaxRetryTimes();
                if (maxRetryTimes == null || maxRetryTimes <= 0) {
                    maxRetryTimes = 1;
                }
                for (int i = 0; i < maxRetryTimes; i++) {
                    try {
                        groovyScriptExecutorService.preInvoke(scriptContext);
                    } catch (RetryInvokeException e) {
                        logger.error("[sms.send]前置处理异常：", e);
                        continue;
                    }
                    long startTime = System.currentTimeMillis();
                    long endTime = System.currentTimeMillis();
                    duration = (int) (endTime - startTime);
                    logger.info("[sms.send]response:{},requestTime:{}ms", response, duration);
                    try {
                        response = request(scriptContext);
                        //解析响应
                        scriptContext.setResponse(response);
                        success = (Boolean) groovyScriptExecutorService.postInvoke(scriptContext);
                        if (Boolean.TRUE.equals(success)) {
                            break;
                        }
                    } catch (HttpUtils.SimpleHttpResponseException e) {
                        e.printStackTrace();
                        scriptContext.setResponse(e.getErrorEntity());
                    }
                    if (groovyScriptExecutorService.retry(scriptContext) == Boolean.TRUE) {
                        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(new Random().nextInt(3) + 1));
                    } else {
                        break;
                    }
                }
            } catch (HttpResponseException e) {
                response = String.format("{\"httpStatusCode\":\"%s\",\"reason\":\"%s\"}", e.getStatusCode(), e.getReasonPhrase());
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                response = StringUtils.isNotBlank(response) ? response + "\n" + e.getMessage() : e.getMessage();
            } finally {
                saveAsyncSendLog(smsSendDTO, entity, scriptContext.getParamsMap(), response, duration, success);
            }
        }
    }
}

