package com.leimbag.rabbitmq.provider.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author leimbag
 */
@SpringBootApplication
public class RabbitMqProviderApplication {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMqProviderApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(RabbitMqProviderApplication.class, args);
        logger.info("启动完毕");
    }
}
