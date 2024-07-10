package com.jiang.duckojbackendjudgequestionservice.mq;


import com.jiang.duckojbackendcommon.common.ErrorCode;
import com.jiang.duckojbackendcommon.exception.BusinessException;
import com.jiang.duckojbackendmodel.model.entity.QuestionSubmit;
import com.jiang.duckojbackendmodel.model.enums.QuestionSubmitStatusEnum;
import com.jiang.duckojbackendserviceclient.service.QuestionOpenFeignClient;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

import static com.jiang.duckojbackendcommon.constant.RabbitMQConstant.DLX_DIRECT_QUEUE;

/**
 * 死信队列， 监听死信队列中的信息：
 */
@Component
@Slf4j
public class MyDlxConsumer {

    @Resource
    private QuestionOpenFeignClient questionOpenFeignClient;


    @RabbitListener(queues = {DLX_DIRECT_QUEUE}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("收到消息:{}", message);
        if (StringUtils.isBlank(message)) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息为空");
        }
        long questionSubmitId = Long.parseLong(message);
        QuestionSubmit questionSubmit = questionOpenFeignClient.getQuestionSubmitById(questionSubmitId);
        if (questionSubmit == null) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "提交题目不存在");
        }
        //将判题机状态设置为失败：
        questionSubmit.setSubmitState(QuestionSubmitStatusEnum.FAILED.getValue());
        boolean b = questionOpenFeignClient.updateQuestionSubmitById(questionSubmit);
        if (!b) {
            //拒绝掉；
            log.info("处理死信队列消息失败,对应提交的题目id为:{}", questionSubmit.getId());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新题目提交异常");
        }
        //确认消息
        channel.basicAck(deliveryTag, false);
    }
}
