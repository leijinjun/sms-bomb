/*
 * 版权所有(C)，上海海鼎信息科技有限公司，2022，所有权利保留。
 * 项目名： server-parent
 * 文件名： SpringApplicationUtil.java
 * 模块说明：
 * 修改历史：
 * 2022/6/29 下午11:25 - lei jinjun - 创建。
 */

package com.lei2j.sms.bomb.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 *
 * @author leijinjun
 * @date 2022/6/29
 **/
@Component
public class SpringApplicationUtils implements BeanFactoryPostProcessor, ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * "@PostConstruct"注解标记的类中，由于ApplicationContext还未加载，导致空指针<br>
     * 因此实现BeanFactoryPostProcessor注入ConfigurableListableBeanFactory实现bean的操作
     */
    private static ConfigurableListableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public static ListableBeanFactory getBeanFactory() {
        return Objects.requireNonNull(null == beanFactory ? applicationContext : beanFactory, "not initialized");
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 通过name获取 Bean
     *
     * @param <T>  Bean类型
     * @param name Bean名称
     * @return Bean
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) getBeanFactory().getBean(name);
    }

    /**
     * 通过class获取Bean
     *
     * @param <T>   Bean类型
     * @param clazz Bean类
     * @return Bean对象
     */
    public static <T> T getBean(Class<T> clazz) {
        return getBeanFactory().getBean(clazz);
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     *
     * @param <T>   bean类型
     * @param name  Bean名称
     * @param clazz bean类型
     * @return Bean对象
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getBeanFactory().getBean(name, clazz);
    }

    public static String getProperty(String key){
        final Environment environment = getBeanFactory().getBean(Environment.class);
        return environment.getProperty(key);
    }
}
