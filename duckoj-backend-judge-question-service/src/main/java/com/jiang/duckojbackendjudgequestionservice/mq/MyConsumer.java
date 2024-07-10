package com.jiang.duckojbackendjudgequestionservice.mq;


import com.jiang.duckojbackendcommon.common.ErrorCode;
import com.jiang.duckojbackendcommon.exception.BusinessException;
import com.jiang.duckojbackendjudgequestionservice.judge.JudgeService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

import static com.jiang.duckojbackendcommon.constant.RabbitMQConstant.DIRECT_QUEUE;

/**
 * 消费者消费：
 */
@Component
@Slf4j
public class MyConsumer {


    @Resource
    private JudgeService judgeService;


    //@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag 开启手动确认后，需要根据消息头确认
    @RabbitListener(queues = {DIRECT_QUEUE}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("收到消息:{}", message);
        if (StringUtils.isBlank(message)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息为空");
        }
        long questionSubmitId = Long.parseLong(message);
        try {
            //消息发送成功
            judgeService.doJudge(questionSubmitId);
            channel.basicAck(deliveryTag, false); // 是否批量确认消息
        } catch (Exception e) {
            //消息为空；拒绝；进入死信队列：
            log.error("消费者端处理消息失败");
            channel.basicNack(deliveryTag, false, false); //不重新入队列
        }
    }
}
