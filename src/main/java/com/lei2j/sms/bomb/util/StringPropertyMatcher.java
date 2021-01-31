package com.lei2j.sms.bomb.util;

import org.springframework.data.domain.ExampleMatcher;

/**
 * @author leijinjun
 * @date 2021/1/19
 **/
public class StringPropertyMatcher extends ExampleMatcher.GenericPropertyMatcher {

    public static ExampleMatcher.GenericPropertyMatcher ofIgnoreStringEmpty() {
        StringPropertyMatcher matcher = new StringPropertyMatcher();
        return matcher.transform(ExampleMatcherStringValueTransformer.IGNORE_EMPTY);
    }
}
