package com.jiang.duckojbackendquestionservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.jiang.duckojbackendquestionservice.mapper")
@ComponentScan("com.jiang")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EnableDiscoveryClient //服务发现
@EnableFeignClients(basePackages = {"com.jiang.duckojbackendserviceclient.service"})
public class DuckojBackendQuestionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DuckojBackendQuestionServiceApplication.class, args);
    }

}
