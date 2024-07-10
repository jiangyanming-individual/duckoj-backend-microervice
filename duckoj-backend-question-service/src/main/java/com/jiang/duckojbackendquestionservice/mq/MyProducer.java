package com.jiang.duckojbackendquestionservice.mq;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class MyProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 生产者生产消息
     */
    //todo 需要发送消息
    public void sendMessage(String exchange, String routingKey, String message) {
        log.info("生产者发送了一条消息");
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

}
