package com.lei2j.sms.bomb.dto;

/**
 * @author leijinjun
 * @date 2020/12/21
 **/
public class SmsSendDTO {

    private String phone;

    private String clientIp;

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

    @Override
    public String toString() {
        return "SmsSendDTO{" +
                "phone='" + phone + '\'' +
                ", clientIp='" + clientIp + '\'' +
                '}';
    }
}
