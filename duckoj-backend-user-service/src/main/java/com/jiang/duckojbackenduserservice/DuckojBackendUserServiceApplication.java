package com.jiang.duckojbackenduserservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication()
@MapperScan("com.jiang.duckojbackenduserservice.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.jiang")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.jiang.duckojbackendserviceclient.service"})
public class DuckojBackendUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DuckojBackendUserServiceApplication.class, args);
    }
}
