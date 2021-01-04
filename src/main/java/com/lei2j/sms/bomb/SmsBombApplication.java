package com.lei2j.sms.bomb;

import com.lei2j.sms.bomb.entity.EntityBasePackage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * @author leijinjun
 * @date 2020/11/25
 **/
@SpringBootApplication
@EntityScan(basePackageClasses = {EntityBasePackage.class})
public class SmsBombApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmsBombApplication.class, args);
    }
}