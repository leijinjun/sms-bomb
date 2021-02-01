package com.lei2j.sms.bomb.dto;

/**
 * @author leijinjun
 * @date 2020/12/21
 **/
public class SmsSendDTO {

    private String phone;

    private String clientIp;

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
    public String toString() {
        return "SmsSendDTO{" +
                "phone='" + phone + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}
