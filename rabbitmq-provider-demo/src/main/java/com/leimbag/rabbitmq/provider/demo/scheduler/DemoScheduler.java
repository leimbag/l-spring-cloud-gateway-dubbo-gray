package com.leimbag.rabbitmq.provider.demo.scheduler;

import com.leimbag.rabbitmq.provider.demo.constant.MessageConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author leimbag
 */
@Component
public class DemoScheduler {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("taskAmqpTemplate")
    protected AmqpTemplate amqpTemplate;

    @Scheduled(fixedRate = 60000)
    public void run() {
        String message = "demo@" + new Date();
        amqpTemplate.convertAndSend(MessageConstant.EXCHANGE_NAME_DEFAULT, MessageConstant.ROUTING_KEY_DEMO_TEST, message);
        logger.info("发送消息:{}", message);
    }
}
