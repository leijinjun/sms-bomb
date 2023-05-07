package com.lei2j.sms.bomb.web.controller;

import com.lei2j.sms.bomb.dto.SmsSendDTO;
import com.lei2j.sms.bomb.entity.SmsSendLog;
import com.lei2j.sms.bomb.repository.SmsSendLogRepository;
import com.lei2j.sms.bomb.repository.SmsSendLogRepository.GroupStatus;
import com.lei2j.sms.bomb.service.impl.SmsSendService;
import com.lei2j.sms.bomb.service.impl.SmsUrlConfigServiceImpl;
import com.lei2j.sms.bomb.web.interceptor.ClientIpInterceptor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.*;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author leijinjun
 * @date 2020/11/29
 **/
@RestController
public class SmsBombController {

    private final Pattern phonePattern = Pattern.compile("^1[3456789]\\d{9}$");

    private final SmsSendService smsSendService;

    private final SmsSendLogRepository smsSendLogRepository;

    @Value("${smb.bomb.phone.send.day.max.count}")
    private Integer maxSendCount;

    @Value("${smb.bomb.send.window.size}")
    private Integer sendSize;

    @Autowired
    private SmsUrlConfigServiceImpl smsUrlConfigService;

    @Autowired
    public SmsBombController(SmsSendService smsSendService, SmsSendLogRepository smsSendLogRepository) {
        this.smsSendService = smsSendService;
        this.smsSendLogRepository = smsSendLogRepository;
    }

    @PostMapping("/smsBomb/send")
    public ResponseEntity<String> send(@RequestParam("phone") String phone,
                                       @RequestParam("sendItems") Integer sendItems,
                                       HttpServletRequest request) {
        if (!phonePattern.asPredicate().test(phone)) {
            return ResponseEntity.badRequest().build();
        }
        String clientIp = String.valueOf(request.getAttribute(ClientIpInterceptor.CLIENT_IP));
        /*LocalDateTime now = LocalDateTime.now(ZoneId.of("CTT", ZoneId.SHORT_IDS));
        LocalDateTime before = now.with(ChronoField.HOUR_OF_DAY, 0).with(ChronoField.MINUTE_OF_HOUR, 0).with(ChronoField.SECOND_OF_MINUTE, 0);
        LocalDateTime after = now.with(ChronoField.HOUR_OF_DAY, 23).with(ChronoField.MINUTE_OF_HOUR, 59).with(ChronoField.SECOND_OF_MINUTE, 59);
        long count = smsSendLogRepository.countByPhoneEqualsAndCreateAtBetween(phone, before, after);
        if (count >= maxSendCount) {
            return ResponseEntity.unprocessableEntity().body("手机号受限");
        }
        long ipCount = smsSendLogRepository.countByIpEqualsAndCreateAtBetween(clientIp, before, after);
        if (ipCount >= maxSendCount) {
            return ResponseEntity.unprocessableEntity().body("IP受限");
        }*/
        if (sendItems == null) {
            sendItems = maxSendCount;
        } else {
            sendItems = Math.min(sendItems, maxSendCount);
        }
        String requestId = System.nanoTime() + "00" + RandomStringUtils.randomNumeric(7);
        SmsSendDTO smsSendDTO = new SmsSendDTO();
        smsSendDTO.setPhone(phone);
        smsSendDTO.setClientIp(clientIp);
        smsSendDTO.setRequestId(requestId);
        smsSendDTO.setSendItems(sendItems);
        final boolean send = smsSendService.send(smsSendDTO);
        return send ? ResponseEntity.ok(requestId) : ResponseEntity.unprocessableEntity().body("没有可用的短信API资源");
    }

    @RequestMapping("/smsBomb/shutdown/{id}")
    public ResponseEntity<Void> shutdown(@PathVariable("id") String id) {
        smsSendService.shutdown(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/smsBomb/getResult")
    public ResponseEntity<Object[]> getResult(String requestId,
                                              int sendSize) throws InterruptedException {
        if (StringUtils.isBlank(requestId)) {
            return ResponseEntity.badRequest().build();
        }
        Object[] result = new Object[]{0, 0};
        Boolean finished = smsSendService.isFinished(requestId, sendSize);
        result[0] = Objects.equals(true, finished) ? 1 : 0;
        TimeUnit.SECONDS.sleep(1);
        List<GroupStatus> mapList = smsSendLogRepository.groupByResponseStatus(requestId);
        int totalCount = mapList.stream().mapToInt(GroupStatus::getTotalCount).sum();
        result[1] =
                mapList.stream().filter(p -> Objects.equals(p.getResponseStatus(), SmsSendLog.SUCCESS_STATUS)).mapToInt(GroupStatus::getTotalCount).sum();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/smsBomb/getSize")
    public ResponseEntity<Object> getSize(int size) throws InterruptedException {
        return ResponseEntity.ok(smsUrlConfigService.get(size));
    }
}
