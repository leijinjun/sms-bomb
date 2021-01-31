package com.lei2j.sms.bomb.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author leijinjun
 * @date 2020/11/29
 **/
@Table(name = "s_sms_url")
@Entity
public class SmsUrlConfig {

    public static final Pattern HEADER_COMPILE = Pattern.compile("\\[.+?]");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "website_name")
    private String websiteName;

    @Column(name = "icon")
    private String icon;

    @Column(name = "website")
    private String website;

    @Column(name = "sms_url",nullable = false)
    private String smsUrl;

    @Column(name = "phone_param_name",nullable = false)
    private String phoneParamName;

    @Column(name = "binding_params")
    private String bindingParams;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    @CreationTimestamp
    private LocalDateTime updateAt;

    @Column(name = "is_normal")
    private Boolean normal;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "success_code")
    private String successCode;

    @Column(name = "end_code")
    private String endCode;

    @Column(name = "script_name")
    private String scriptName;

    @Column(name = "script_content")
    private String scriptContent;

    @Column(name = "script_path")
    private String scriptPath;

    @Column(name = "request_method")
    private String requestMethod;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "headers")
    private String headers;

    @Column(name = "open_script")
    private Boolean openScript;

    private String responseType;

    @Column(name = "last_used_time")
    private LocalDateTime lastUsedTime;

    public SmsUrlConfig() {
    }

    public SmsUrlConfig(Integer id, String websiteName, String icon, String website, String smsUrl, String phoneParamName, String bindingParams, LocalDateTime createAt, LocalDateTime updateAt, Boolean normal, String businessName, String successCode,
                        String endCode, String scriptName, String scriptContent, String scriptPath, String requestMethod, String contentType, String headers, Boolean openScript, String responseType, LocalDateTime lastUsedTime) {
        this.id = id;
        this.websiteName = websiteName;
        this.icon = icon;
        this.website = website;
        this.smsUrl = smsUrl;
        this.phoneParamName = phoneParamName;
        this.bindingParams = bindingParams;
        this.createAt = createAt;
        this.updateAt = updateAt;
        this.normal = normal;
        this.businessName = businessName;
        this.successCode = successCode;
        this.endCode = endCode;
        this.scriptName = scriptName;
        this.scriptContent = scriptContent;
        this.scriptPath = scriptPath;
        this.requestMethod = requestMethod;
        this.contentType = contentType;
        this.headers = headers;
        this.openScript = openScript;
        this.responseType = responseType;
        this.lastUsedTime = lastUsedTime;
    }

    public List<String> getHeaderList(){
        List<String> headerList = new ArrayList<>();
        //解析固定请求头
        if (StringUtils.isNotBlank(headers)) {
            Matcher matcher = SmsUrlConfig.HEADER_COMPILE.matcher(headers);
            while (matcher.find()) {
                String group = matcher.group();
                String headerPair = group.substring(1, group.length() - 1);
                headerList.add(headerPair);
            }
        }
        return headerList;
    }

    public Map<String, String> getBindingParamsMap() {
        if (StringUtils.isNotBlank(bindingParams)) {
            return JSON.parseObject(bindingParams, new TypeReference<LinkedHashMap<String, String>>() {
            });
        }
        return Collections.emptyMap();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSmsUrl() {
        return smsUrl;
    }

    public void setSmsUrl(String smsUrl) {
        this.smsUrl = smsUrl;
    }

    public String getPhoneParamName() {
        return phoneParamName;
    }

    public void setPhoneParamName(String phoneParamName) {
        this.phoneParamName = phoneParamName;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }

    public Boolean getNormal() {
        return normal;
    }

    public void setNormal(Boolean normal) {
        this.normal = normal;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getSuccessCode() {
        return successCode;
    }

    public void setSuccessCode(String successCode) {
        this.successCode = successCode;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getScriptContent() {
        return scriptContent;
    }

    public void setScriptContent(String scriptContent) {
        this.scriptContent = scriptContent;
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getBindingParams() {
        return bindingParams;
    }

    public void setBindingParams(String bindingParams) {
        this.bindingParams = bindingParams;
    }

    public Boolean getOpenScript() {
        return openScript;
    }

    public void setOpenScript(Boolean openScript) {
        this.openScript = openScript;
    }

    public LocalDateTime getLastUsedTime() {
        return lastUsedTime;
    }

    public void setLastUsedTime(LocalDateTime lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getWebsiteName() {
        return websiteName;
    }

    public void setWebsiteName(String websiteName) {
        this.websiteName = websiteName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getEndCode() {
        return endCode;
    }

    public void setEndCode(String endCode) {
        this.endCode = endCode;
    }

    @Override
    public String toString() {
        return "SmsUrlConfig{" +
                "id=" + id +
                ", websiteName='" + websiteName + '\'' +
                ", icon='" + icon + '\'' +
                ", website='" + website + '\'' +
                ", smsUrl='" + smsUrl + '\'' +
                ", phoneParamName='" + phoneParamName + '\'' +
                ", bindingParams='" + bindingParams + '\'' +
                ", createAt=" + createAt +
                ", updateAt=" + updateAt +
                ", normal=" + normal +
                ", businessName='" + businessName + '\'' +
                ", successCode='" + successCode + '\'' +
                ", endCode='" + endCode + '\'' +
                ", scriptName='" + scriptName + '\'' +
                ", scriptContent='" + scriptContent + '\'' +
                ", scriptPath='" + scriptPath + '\'' +
                ", requestMethod='" + requestMethod + '\'' +
                ", contentType='" + contentType + '\'' +
                ", headers='" + headers + '\'' +
                ", openScript=" + openScript +
                ", responseType='" + responseType + '\'' +
                ", lastUsedTime=" + lastUsedTime +
                '}';
    }
}
