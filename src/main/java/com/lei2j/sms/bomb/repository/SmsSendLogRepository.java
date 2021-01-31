package com.lei2j.sms.bomb.repository;

import com.lei2j.sms.bomb.entity.SmsSendLog;

import java.time.LocalDateTime;

/**
 * @author leijinjun
 * @version V1.0
 * @date 2021/1/4
 **/
public interface SmsSendLogRepository extends CommonJpaRepository<SmsSendLog,Integer> {

    long countByPhoneEqualsAndCreateAtBetween(String phone, LocalDateTime before, LocalDateTime after);

    long countByIpEqualsAndCreateAtBetween(String ip, LocalDateTime before, LocalDateTime after);
}
