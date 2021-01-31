package com.lei2j.sms.bomb.web.config;

import com.lei2j.sms.bomb.web.interceptor.ClientIpInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

/**
 * @author leijinjun
 * @date 2020/12/22
 **/
@Configuration
//此注解会导致一些web mvc配置失效，如默认首页无法找到，也不能继承{@code WebMvcConfigurationSupport}
//@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private Environment environment;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(new ClientIpInterceptor());
        String staticBase = environment.getProperty("mvc.static.resource.base");
        if (StringUtils.hasText(staticBase)) {
            registration.excludePathPatterns(staticBase + "/**");
        }
    }

    @Resource
    private void setAttrServletContext(WebApplicationContext attrServletContext){
        ServletContext servletContext = attrServletContext.getServletContext();
        String contextPath = environment.getProperty("server.servlet.context-path");
        String staticResourcePath = environment.getProperty("mvc.static.resource.base");
        String staticPath = "";
        if (StringUtils.hasText(contextPath)) {
            staticPath += contextPath;
        }
        if (StringUtils.hasText(staticResourcePath)) {
            staticPath += staticResourcePath;
        }
        assert servletContext != null;
        servletContext.setAttribute("smsBombStaticPath", staticPath);
    }
}
