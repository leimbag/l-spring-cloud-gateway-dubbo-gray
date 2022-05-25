package com.leimbag.rabbitmq.consumer.demo.config;

import com.leimbag.rabbitmq.consumer.demo.component.rabbitmq.TraceMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author leimbag
 */
@Configuration
public class RabbitMqConsumerTraceConfig {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SimpleMessageListenerContainer simpleMessageListenerContainer;

    @PostConstruct
    public void init(){
        logger.info("重置消息转换器");
        MessageListenerAdapter messageListenerAdapter = (MessageListenerAdapter) simpleMessageListenerContainer.getMessageListener();
        messageListenerAdapter.setMessageConverter(new TraceMessageConverter());
        logger.info("重置完成");
    }
}
