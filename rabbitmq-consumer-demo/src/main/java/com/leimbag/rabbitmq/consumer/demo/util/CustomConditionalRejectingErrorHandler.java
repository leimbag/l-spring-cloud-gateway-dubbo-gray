package com.leimbag.rabbitmq.consumer.demo.util;

import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;

/**
 * @author leimbag
 */
public class CustomConditionalRejectingErrorHandler extends ConditionalRejectingErrorHandler {
    protected void log(Throwable t) {
        this.logger.error("Execution of Rabbit message listener failed.", t);
    }
}
