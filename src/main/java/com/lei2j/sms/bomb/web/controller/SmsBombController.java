package com.lei2j.sms.bomb.web.controller;

import com.lei2j.sms.bomb.repository.SmsUrlConfigRepository;
import com.lei2j.sms.bomb.service.impl.SmsSendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

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
}
