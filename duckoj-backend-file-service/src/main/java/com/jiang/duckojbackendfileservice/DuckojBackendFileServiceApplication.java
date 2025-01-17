package com.jiang.duckojbackendfileservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 文件服务：
 */
@SpringBootApplication()
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.jiang")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.jiang.duckojbackendserviceclient.service"})
public class DuckojBackendFileServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DuckojBackendFileServiceApplication.class, args);
    }

}
