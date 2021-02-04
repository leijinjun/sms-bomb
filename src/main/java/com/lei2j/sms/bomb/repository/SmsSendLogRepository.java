package com.lei2j.sms.bomb.repository;

import com.lei2j.sms.bomb.entity.SmsSendLog;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author leijinjun
 * @version V1.0
 * @date 2021/1/4
 **/
public interface SmsSendLogRepository extends CommonJpaRepository<SmsSendLog,Integer> {

     interface GroupStatus{

        Integer getTotalCount();

        String getResponseStatus();
    }

    long countByPhoneEqualsAndCreateAtBetween(String phone, LocalDateTime before, LocalDateTime after);

    long countByIpEqualsAndCreateAtBetween(String ip, LocalDateTime before, LocalDateTime after);

    @Query(value ="select count(responseStatus) as totalCount,responseStatus as responseStatus from SmsSendLog where requestId =?1 group by responseStatus")
    List<GroupStatus> groupByResponseStatus(String requestId);

    long countByRequestIdEqualsAndResponseStatusEquals(String requestId, String responseStatus);
}
