package com.lei2j.sms.bomb.web.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author leijinjun
 * @date 2020/12/22
 **/
public class ClientIpInterceptor implements HandlerInterceptor {

    public static final String CLIENT_IP = "client_ip";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientIp;
        String ip = request.getParameter("X-Forwarded-For");
        if (StringUtils.isNotBlank(ip) && isNotUnknown(ip)) {
            clientIp = ip.split(",", 1)[0];
        } else if (StringUtils.isNotBlank(ip = request.getParameter("X-Real-IP")) && isNotUnknown(ip)) {
            clientIp = ip;
        } else if (StringUtils.isNotBlank(ip = request.getParameter("Proxy-Client-IP")) && isNotUnknown(ip)) {
            clientIp = ip;
        } else {
            clientIp = request.getRemoteAddr();
        }
        request.setAttribute(CLIENT_IP, clientIp);
        return true;
    }

    private boolean isNotUnknown(String ip) {
        return !"unknown".equalsIgnoreCase(ip);
    }
}
