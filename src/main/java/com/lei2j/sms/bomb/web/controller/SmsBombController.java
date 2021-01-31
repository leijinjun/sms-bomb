package com.lei2j.sms.bomb.web.controller;

import com.lei2j.sms.bomb.dto.SmsSendDTO;
import com.lei2j.sms.bomb.repository.SmsSendLogRepository;
import com.lei2j.sms.bomb.service.impl.SmsSendService;
import com.lei2j.sms.bomb.web.interceptor.ClientIpInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.*;
import java.time.temporal.ChronoField;
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

    @Autowired
    public SmsBombController(SmsSendService smsSendService,SmsSendLogRepository smsSendLogRepository) {
        this.smsSendService = smsSendService;
        this.smsSendLogRepository = smsSendLogRepository;
    }

    @PostMapping("/smsBomb/send")
    public ResponseEntity<String> send(@RequestParam("phone")String phone,
                                     HttpServletRequest request){
        if (!phonePattern.asPredicate().test(phone)) {
            return ResponseEntity.badRequest().build();
        }
        String clientIp = String.valueOf(request.getAttribute(ClientIpInterceptor.CLIENT_IP));
        LocalDateTime now = LocalDateTime.now(ZoneId.of("CTT", ZoneId.SHORT_IDS));
        LocalDateTime before = now.with(ChronoField.HOUR_OF_DAY, 0).with(ChronoField.MINUTE_OF_HOUR, 0).with(ChronoField.SECOND_OF_MINUTE, 0);
        LocalDateTime after = now.with(ChronoField.HOUR_OF_DAY, 23).with(ChronoField.MINUTE_OF_HOUR, 59).with(ChronoField.SECOND_OF_MINUTE, 59);
        long count = smsSendLogRepository.countByPhoneEqualsAndCreateAtBetween(phone, before, after);
        if (count >= maxSendCount) {
            return ResponseEntity.unprocessableEntity().body("手机号受限");
        }
        long ipCount = smsSendLogRepository.countByIpEqualsAndCreateAtBetween(clientIp, before, after);
        if (ipCount >= maxSendCount) {
            return ResponseEntity.unprocessableEntity().body("IP受限");
        }
        SmsSendDTO smsSendDTO = new SmsSendDTO();
        smsSendDTO.setPhone(phone);
        smsSendDTO.setClientIp(clientIp);
        smsSendService.send(smsSendDTO);
        return ResponseEntity.ok().build();
    }

}
