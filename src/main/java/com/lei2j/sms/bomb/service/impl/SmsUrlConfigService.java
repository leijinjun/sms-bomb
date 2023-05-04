package com.lei2j.sms.bomb.service.impl;

import com.lei2j.sms.bomb.entity.SmsUrlConfig;

import java.util.List;

/**
 * @author leijinjun
 * @date 2023/5/4
 **/
public interface SmsUrlConfigService {

    /**
     * 查询所有
     * @return
     */
    List<SmsUrlConfig> findAll();

    /**
     * 查询指定数量列表
     * @param size
     * @return
     */
    List<SmsUrlConfig> get(int size);
}
