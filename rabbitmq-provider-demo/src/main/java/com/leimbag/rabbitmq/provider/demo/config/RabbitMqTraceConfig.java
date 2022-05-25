package com.leimbag.rabbitmq.provider.demo.config;

import com.leimbag.demo.core.constant.GatewayConstant;
import com.leimbag.demo.core.util.IdGenerateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;

/**
 * @author leimbag
 */
@Configuration
public class RabbitMqTraceConfig implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        Map<String, AmqpTemplate> amqpTemplateMap = applicationContext.getBeansOfType(AmqpTemplate.class);
        for (Map.Entry<String, AmqpTemplate> entry : amqpTemplateMap.entrySet()) {
            RabbitTemplate rabbitTemplate = (RabbitTemplate) entry.getValue();
            rabbitTemplate.addBeforePublishPostProcessors(new MessagePostProcessor() {
                private final Logger logger = LoggerFactory.getLogger(this.getClass());

                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    // 添加GID
                    String gid = MDC.get(GatewayConstant.GID);
                    if (Objects.isNull(gid)) {
                        gid = IdGenerateUtil.generateId();
                        logger.info("生产者gid:{}", gid);
                    }
                    message.getMessageProperties().setHeader(GatewayConstant.GID, gid);
                    return message;
                }
            });
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
