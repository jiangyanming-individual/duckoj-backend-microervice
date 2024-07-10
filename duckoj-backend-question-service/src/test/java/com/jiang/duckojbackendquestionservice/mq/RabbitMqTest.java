package com.jiang.duckojbackendquestionservice.mq;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static com.jiang.duckojbackendcommon.constant.RabbitMQConstant.*;

@SpringBootTest
public class RabbitMqTest {


    @Resource
    private MyProducer myProducer;


    @Test
    public void testMQ() {

        String exchange = DIRECT_EXCHANGE;
        String queue = DIRECT_QUEUE;
        String routing_key = ROUTING_KEY;
        myProducer.sendMessage(exchange, routing_key, "你好！");
    }
}
