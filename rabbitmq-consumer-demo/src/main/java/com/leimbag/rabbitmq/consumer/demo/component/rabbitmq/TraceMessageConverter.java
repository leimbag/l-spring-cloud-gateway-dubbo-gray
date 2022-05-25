package com.leimbag.rabbitmq.consumer.demo.component.rabbitmq;

import com.leimbag.demo.core.constant.GatewayConstant;
import com.leimbag.demo.core.util.LogHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.SimpleMessageConverter;

/**
 * @author leimbag
 */
public class TraceMessageConverter extends SimpleMessageConverter {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        logger.info("接收消息：{}", LogHelper.toJsonWithObject(message));
        MessageProperties properties = message.getMessageProperties();
        if (properties != null) {
            String gid = properties.getHeader(GatewayConstant.GID);
            if (StringUtils.isNotBlank(gid)) {
                logger.info("接收gid={}的消息", gid);
                MDC.put(GatewayConstant.GID, gid);
            }
        }
        return super.fromMessage(message);
    }
}
