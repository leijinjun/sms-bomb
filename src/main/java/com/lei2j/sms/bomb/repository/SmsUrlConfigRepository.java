package com.lei2j.sms.bomb.repository;

import com.lei2j.sms.bomb.entity.SmsUrlConfig;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

/**
 * @author leijinjun
 * @version V1.0
 * @date 2021/1/4
 **/
public interface SmsUrlConfigRepository extends CommonJpaRepository<SmsUrlConfig, Long> {

    /**
     * @param normal
     * @return
     */
    List<SmsUrlConfig> findByNormalEquals(Boolean normal);

    Integer countByNormalEquals(Boolean normal);

    @Query(nativeQuery = true,value = "select s.* from s_sms_url s where s.is_normal =?1 order by s.last_used_time limit ?2")
    List<SmsUrlConfig> findTopListByNormalEquals(Boolean normal,Integer size);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "update s_sms_url set is_normal=?1")
    void updateAllStatus(Boolean normal);
}