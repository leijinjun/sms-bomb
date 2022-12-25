package com.lei2j.sms.bomb.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.ExampleMatcher;

import java.util.Optional;

/**
 * @author leijinjun
 * @date 2021/1/19
 **/
public enum IgnoreEmptyStringValueTransformer implements ExampleMatcher.PropertyValueTransformer{

    /**
     * 忽略空字符串
     */
    IGNORE_EMPTY;


    @Override
    public Optional<Object> apply(Optional<Object> source) {
        boolean present = source.isPresent();
        if (!present) {
            return source;
        }
        return source.filter(p -> {
            if (p instanceof String) {
                return StringUtils.trimToNull((String) p) != null;
            }
            return true;
        });
    }
}
