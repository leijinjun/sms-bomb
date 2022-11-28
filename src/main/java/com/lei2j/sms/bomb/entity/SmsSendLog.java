package com.lei2j.sms.bomb.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author leijinjun
 * @date 2020/12/21
 **/
@Entity
@Table(name = "s_sms_send_log")
public class SmsSendLog {

    public static final String SUCCESS_STATUS = "SUCCESS";
    public static final String FAILURE_STATUS = "FAILURE";

    @Id
    private Long id;

    private String phone;

    private String ip;

    @Column(name = "sms_url")
    private String smsUrl;

    @Column(name = "web_site_name")
    private String webSiteName;

    private String params;

    private String response;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "request_duration")
    private Integer requestDuration;

    /**
     * @see #SUCCESS_STATUS
     * @see #FAILURE_STATUS
     */
    @Column(name = "response_status")
    private String responseStatus;

    private String requestId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getSmsUrl() {
        return smsUrl;
    }

    public void setSmsUrl(String smsUrl) {
        this.smsUrl = smsUrl;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public Integer getRequestDuration() {
        return requestDuration;
    }

    public void setRequestDuration(Integer requestDuration) {
        this.requestDuration = requestDuration;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getWebSiteName() {
        return webSiteName;
    }

    public void setWebSiteName(String webSiteName) {
        this.webSiteName = webSiteName;
    }
}
