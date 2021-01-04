package com.lei2j.sms.bomb.repository;

import com.lei2j.sms.bomb.entity.SmsSendLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author leijinjun
 * @version V1.0
 * @date 2021/1/4
 **/
public interface SmsSendLogRepository extends JpaRepository<SmsSendLog,Integer> {
}
