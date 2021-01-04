package com.lei2j.sms.bomb.web.controller;

import com.lei2j.sms.bomb.dto.SmsSendDTO;
import com.lei2j.sms.bomb.entity.SmsUrlConfig;
import com.lei2j.sms.bomb.repository.SmsUrlConfigRepository;
import com.lei2j.sms.bomb.service.impl.SmsSendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author leijinjun
 * @date 2020/11/29
 **/
@RestController
public class SmsBombController {

    @Autowired
    private SmsUrlConfigRepository smsUrlConfigRepository;
    @Autowired
    private SmsSendService smsSendService;

    @PostConstruct
    private void init() {
        List<SmsUrlConfig> list = smsUrlConfigRepository.findTopListByNormalEquals(Boolean.TRUE, 10);
        SmsSendDTO smsSendDTO = new SmsSendDTO();
        smsSendDTO.setPhone("18915255662");
        smsSendService.send(smsSendDTO);
    }
}
