package com.shixin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy=true)
public class OrderManageApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderManageApplication.class, args);
    }
}
