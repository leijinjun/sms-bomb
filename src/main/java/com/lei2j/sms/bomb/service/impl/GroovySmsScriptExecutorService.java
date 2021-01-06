package com.lei2j.sms.bomb.service.impl;

import com.lei2j.sms.bomb.entity.SmsUrlConfig;
import com.lei2j.sms.bomb.script.SmsCommonScript;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author leijinjun
 * @date 2020/12/17
 **/
@Configuration
@Service
public class GroovySmsScriptExecutorService {

    private static final GroovyClassLoader LOADER = new GroovyClassLoader();

    private static final Map<String, ScriptCache> SCRIPT_CACHE = new ConcurrentHashMap<>(64);

    private static final Map<String,GroovyObject> INSTANCE_CACHE = new ConcurrentHashMap<>(64);

    static {
        URL resource = GroovySmsScriptExecutorService.class.getResource("/script");
        LOADER.addURL(resource);
    }

    @Value("${smb.bomb.script.base.path}")
    private String groovyScriptBasePath;

    private GroovyObject parse(SmsUrlConfig smsUrlConfig, String base) throws IOException, IllegalAccessException, InstantiationException {
        String content = smsUrlConfig.getScriptContent();
        Boolean openScript = smsUrlConfig.getOpenScript();
        Class<?> cls = null;
        if (Boolean.TRUE.equals(openScript)) {
            if (StringUtils.isNotBlank(content)) {
                cls = parseContent(content, smsUrlConfig.getScriptName());
            } else if (StringUtils.isNotBlank(smsUrlConfig.getScriptPath())) {
                String path = smsUrlConfig.getScriptPath();
                Path filePath = Paths.get(base, path);
                BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
                cls = LOADER.parseClass(reader, path);
            }
        }
        if (cls == null) {
            return new SmsCommonScript();
        }
        return (GroovyObject) cls.newInstance();
    }

    private Class<?> parseContent(final String content, final String name) {
        final String checkDigit = DigestUtils.md5DigestAsHex(content.getBytes(StandardCharsets.UTF_8));
        ScriptCache scriptCache;
        if (!SCRIPT_CACHE.containsKey(name) || !(scriptCache = SCRIPT_CACHE.get(name)).checkDigit(checkDigit)) {
            Class<?> cls = LOADER.parseClass(content, name);
            scriptCache = new ScriptCache(cls, checkDigit);
            SCRIPT_CACHE.put(name, scriptCache);
        }
        return scriptCache.getCls();
    }

    private Object preInvoke0(GroovyObject instance, String method, Object... args) {
        Objects.requireNonNull(instance);
        return instance.invokeMethod(method, args);
    }

    public void preInvoke(SmsUrlConfig smsUrlConfig, Map<String, Object> paramsMap, Map<String, String> headerMap) throws Exception {
        preInvoke0(parse(smsUrlConfig, groovyScriptBasePath), "preProcess", smsUrlConfig, paramsMap, headerMap);
    }

    public Object postInvoke(SmsUrlConfig smsUrlConfig, String response) throws Exception {
        return preInvoke0(parse(smsUrlConfig, groovyScriptBasePath), "postProcess", smsUrlConfig, response);
    }

    private static class ScriptCache {
        private Class<?> cls;

        private String checkDigit;

        public ScriptCache() {
        }

        public ScriptCache(Class<?> cls, String checkDigit) {
            this.cls = cls;
            this.checkDigit = checkDigit;
        }

        public boolean checkDigit(String checkDigit) {
            return this.checkDigit.equals(checkDigit);
        }

        public Class<?> getCls() {
            return cls;
        }

        public void setCls(Class<?> cls) {
            this.cls = cls;
        }

        public String getCheckDigit() {
            return checkDigit;
        }

        public void setCheckDigit(String checkDigit) {
            this.checkDigit = checkDigit;
        }
    }
}
