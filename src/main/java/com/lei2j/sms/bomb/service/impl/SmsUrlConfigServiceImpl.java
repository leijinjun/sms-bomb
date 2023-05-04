package com.lei2j.sms.bomb.service.impl;

import com.lei2j.sms.bomb.entity.SmsUrlConfig;
import com.lei2j.sms.bomb.repository.SmsUrlConfigRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author leijinjun
 * @date 2023/5/4
 **/
@Service
public class SmsUrlConfigServiceImpl implements SmsUrlConfigService {

    private final SmsUrlConfigRepository smsUrlConfigRepository;

    public SmsUrlConfigServiceImpl(SmsUrlConfigRepository smsUrlConfigRepository) {
        this.smsUrlConfigRepository = smsUrlConfigRepository;
    }

    @Override
    public List<SmsUrlConfig> findAll() {
        return smsUrlConfigRepository.findByNormalEquals(true);
    }

    @Override
    public List<SmsUrlConfig> get(int size) {
        final List<SmsUrlConfig> smsUrlConfigs = findAll();
        final int length = smsUrlConfigs.size();
        final Set<Integer> set = rand(length, size, null);
        List<SmsUrlConfig> result = new ArrayList<>();
        for (Integer index : set) {
            result.add(smsUrlConfigs.get(index));
        }
        return result;
    }

    private Set<Integer> rand(int max, int size, Set<Integer> set) {
        final Random random = new Random();
        if (set == null) {
            set = new HashSet<>();
        }
        size = Math.min(max, size);
        final int nextInt = random.nextInt(max);
        set.add(nextInt);
        if (set.size() >= size) {
            return set;
        } else {
            return rand(max, size, set);
        }
    }
}
