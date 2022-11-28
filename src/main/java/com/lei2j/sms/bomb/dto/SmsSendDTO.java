package com.lei2j.sms.bomb.dto;

import java.util.Objects;

/**
 * @author leijinjun
 * @date 2020/12/21
 **/
public class SmsSendDTO {

    /**
     * 手机号
     */
    private String phone;

    /**
     * 客户端ip
     */
    private String clientIp;

    /**
     * 请求id
     */
    private String requestId;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SmsSendDTO that = (SmsSendDTO) o;
        return Objects.equals(requestId, that.requestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId);
    }

    @Override
    public String toString() {
        return "SmsSendDTO{" +
                "phone='" + phone + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}
